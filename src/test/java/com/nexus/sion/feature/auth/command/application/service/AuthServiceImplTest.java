package com.nexus.sion.feature.auth.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.auth.command.application.dto.request.LoginRequest;
import com.nexus.sion.feature.auth.command.application.dto.response.TokenResponse;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.repository.MemberRepository;
import com.nexus.sion.security.jwt.JwtTokenProvider;

class AuthServiceImplTest {

  @Mock private MemberRepository memberRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtTokenProvider jwtTokenProvider;

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
}
