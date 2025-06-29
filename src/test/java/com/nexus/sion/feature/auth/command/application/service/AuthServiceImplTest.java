package com.nexus.sion.feature.auth.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.auth.command.application.dto.request.LoginRequest;
import com.nexus.sion.feature.auth.command.application.dto.response.TokenResponse;
import com.nexus.sion.feature.auth.command.domain.aggregate.RefreshToken;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.security.jwt.JwtTokenProvider;

class AuthServiceImplTest {

  @Mock private MemberRepository memberRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private RedisTemplate<String, RefreshToken> redisTemplate;

  @Mock private ValueOperations<String, RefreshToken> valueOperations;

  @InjectMocks private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void login_Success() {
    // given
    String id = "EMP123";
    String rawPassword = "1234";
    String encodedPassword = "encodedPW";
    String accessToken = "access.token";
    String refreshToken = "refresh.token";

    LoginRequest request = new LoginRequest(id, rawPassword);

    Member member =
        Member.builder()
            .employeeIdentificationNumber(id)
            .password(encodedPassword)
            .role(MemberRole.INSIDER)
            .build();

    when(memberRepository.findById(id)).thenReturn(Optional.of(member));
    when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
    when(jwtTokenProvider.createToken(id, "INSIDER")).thenReturn(accessToken);
    when(jwtTokenProvider.createRefreshToken(id, "INSIDER")).thenReturn(refreshToken);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // when
    TokenResponse response = authService.login(request);

    // then
    assertThat(response.getAccessToken()).isEqualTo(accessToken);
    assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
  }

  @Test
  void login_UserNotFound_ThrowsException() {
    // given
    String id = "EMP123";
    LoginRequest request = new LoginRequest(id, "1234");

    when(memberRepository.findById(id)).thenReturn(Optional.empty());

    // when & then
    BusinessException exception =
        assertThrows(BusinessException.class, () -> authService.login(request));
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  void login_InvalidPassword_ThrowsException() {
    // given
    String id = "EMP123";
    String rawPassword = "1234";
    String encodedPassword = "encodedPW";

    LoginRequest request = new LoginRequest(id, rawPassword);

    Member member =
        Member.builder()
            .employeeIdentificationNumber(id)
            .password(encodedPassword)
            .role(MemberRole.INSIDER)
            .build();

    when(memberRepository.findById(id)).thenReturn(Optional.of(member));
    when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

    // when & then
    BusinessException exception =
        assertThrows(BusinessException.class, () -> authService.login(request));
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURRENT_PASSWORD);
  }

  @Test
  void refreshToken_success() {
    // given
    String providedRefreshToken = "provided-refresh-token";
    String employeeId = "EMP123";
    String accessToken = "new-access-token";
    String newRefreshToken = "new-refresh-token";

    Member member =
        Member.builder()
            .employeeIdentificationNumber(employeeId)
            .role(MemberRole.ADMIN) // 너희 프로젝트 enum에 따라 수정
            .build();

    RefreshToken storedRefreshToken = RefreshToken.builder().token(providedRefreshToken).build();

    // mock 설정
    when(jwtTokenProvider.validateToken(providedRefreshToken)).thenReturn(true);
    when(jwtTokenProvider.getEmployeeIdentificationNumberFromJwt(providedRefreshToken))
        .thenReturn(employeeId);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(employeeId)).thenReturn(storedRefreshToken);
    when(memberRepository.findByEmployeeIdentificationNumberAndDeletedAtIsNull(employeeId))
        .thenReturn(Optional.of(member));
    when(jwtTokenProvider.createToken(employeeId, "ADMIN")).thenReturn(accessToken);
    when(jwtTokenProvider.createRefreshToken(employeeId, "ADMIN")).thenReturn(newRefreshToken);

    // when
    TokenResponse response = authService.refreshToken(providedRefreshToken);

    // then
    assertNotNull(response);
    assertEquals(accessToken, response.getAccessToken());
    assertEquals(newRefreshToken, response.getRefreshToken());

    // Redis set 검증
    verify(valueOperations).set(eq(employeeId), any(RefreshToken.class), eq(Duration.ofDays(7)));
  }

  @DisplayName("리프레시 토큰이 Redis에 없을 경우 예외 발생")
  @Test
  void refreshToken_리프레시토큰없음_예외() {
    // given
    String providedRefreshToken = "any-token";
    String employeeId = "EMP001";

    when(jwtTokenProvider.validateToken(providedRefreshToken)).thenReturn(true);
    when(jwtTokenProvider.getEmployeeIdentificationNumberFromJwt(providedRefreshToken))
        .thenReturn(employeeId);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(employeeId)).thenReturn(null); // Redis에 없음

    // when & then
    assertThrows(
        BadCredentialsException.class,
        () -> {
          authService.refreshToken(providedRefreshToken);
        });
  }

  @DisplayName("리프레시 토큰이 Redis에 있지만 값이 다르면 예외 발생")
  @Test
  void refreshToken_리프레시토큰불일치_예외() {
    // given
    String providedRefreshToken = "incoming-token";
    String employeeId = "EMP002";
    String storedToken = "stored-token";

    RefreshToken redisToken = RefreshToken.builder().token(storedToken).build();

    when(jwtTokenProvider.validateToken(providedRefreshToken)).thenReturn(true);
    when(jwtTokenProvider.getEmployeeIdentificationNumberFromJwt(providedRefreshToken))
        .thenReturn(employeeId);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(employeeId)).thenReturn(redisToken);

    // when & then
    assertThrows(
        BadCredentialsException.class,
        () -> {
          authService.refreshToken(providedRefreshToken);
        });
  }

  @Test
  void logout_success() {
    // given
    String refreshToken = "mockRefreshToken";
    String employeeId = "123456";

    when(jwtTokenProvider.getEmployeeIdentificationNumberFromJwt(refreshToken))
        .thenReturn(employeeId);

    // when
    authService.logout(refreshToken);

    // then
    verify(jwtTokenProvider).validateToken(refreshToken);
    verify(jwtTokenProvider).getEmployeeIdentificationNumberFromJwt(refreshToken);
    verify(redisTemplate).delete(employeeId);
  }
}
