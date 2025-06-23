package com.nexus.sion.feature.auth.command.application.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.auth.command.application.dto.request.LoginRequest;
import com.nexus.sion.feature.auth.command.application.dto.response.TokenResponse;
import com.nexus.sion.feature.auth.command.domain.aggregate.RefreshToken;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.repository.MemberRepository;
import com.nexus.sion.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final RedisTemplate<String, RefreshToken> redisTemplate;

  /* 테스트 로그인 */
  @Transactional
  public TokenResponse testLogin() {

    // 토큰 발급
    String accessToken = jwtTokenProvider.createToken("test", MemberRole.ADMIN.name());
    String refreshToken = jwtTokenProvider.createRefreshToken("test", MemberRole.ADMIN.name());

    return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  @Transactional
  public TokenResponse login(LoginRequest loginRequest) {
    Member member =
        memberRepository
            .findById(loginRequest.getEmployeeIdentificationNumber())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
    }

    // 로그인 성공 시 token 발급
    String accessToken =
        jwtTokenProvider.createToken(
            member.getEmployeeIdentificationNumber(), member.getRole().name());
    String refreshToken =
        jwtTokenProvider.createRefreshToken(
            member.getEmployeeIdentificationNumber(), member.getRole().name());

    // Redis에 value로 저장할 객체 생성
    RefreshToken redisRefreshToken = RefreshToken.builder().token(refreshToken).build();

    // Redis에 저장 (TTL: 7일)
    redisTemplate
        .opsForValue()
        .set(member.getEmployeeIdentificationNumber(), redisRefreshToken, Duration.ofDays(7));

    return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  @Override
  public TokenResponse refreshToken(String providedRefreshToken) {
    // 리프레시 토큰 유효성 검사, 저장 되어 있는 userId 추출
    jwtTokenProvider.validateToken(providedRefreshToken);
    String employeeIdentificationNumber =
        jwtTokenProvider.getEmployeeIdentificationNumberFromJwt(providedRefreshToken);

    // Redis에 저장된 리프레시 토큰 조회
    RefreshToken storedRefreshToken = redisTemplate.opsForValue().get(employeeIdentificationNumber);
    if (storedRefreshToken == null) {
      throw new BadCredentialsException("해당 유저로 조회되는 리프레시 토큰 없음");
    }

    // 넘어온 리프레시 토큰과 Redis의 토큰 비교
    if (!storedRefreshToken.getToken().equals(providedRefreshToken)) {
      throw new BadCredentialsException("리프레시 토큰 일치하지 않음");
    }

    // 삭제되지 않은 멤버 조회
    Member member =
        memberRepository
            .findByEmployeeIdentificationNumberAndDeletedAtIsNull(employeeIdentificationNumber)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    String accessToken =
        jwtTokenProvider.createToken(employeeIdentificationNumber, member.getRole().name());
    String refreshToken =
        jwtTokenProvider.createRefreshToken(employeeIdentificationNumber, member.getRole().name());

    RefreshToken newToken = RefreshToken.builder().token(refreshToken).build();

    // Redis에 새로운 리프레시 토큰 저장 ( 기존 토큰 덮어쓰기 )
    redisTemplate
        .opsForValue()
        .set(member.getEmployeeIdentificationNumber(), newToken, Duration.ofDays(7));

    return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }
}
