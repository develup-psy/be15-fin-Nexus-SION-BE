package com.nexus.sion.feature.auth.command;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;
import java.util.Arrays;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.auth.command.application.dto.request.LoginRequest;
import com.nexus.sion.feature.auth.command.application.dto.request.RefreshTokenRequest;
import com.nexus.sion.feature.auth.command.domain.aggregate.RefreshToken;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.repository.MemberRepository;
import com.nexus.sion.security.jwt.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트가 끝나면 DB를 원래 상태로 되돌린다.
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 1개의 인스턴스만 생성
public class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private MemberRepository memberRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private JwtTokenProvider jwtTokenProvider;

  @Autowired private RedisTemplate<String, RefreshToken> redisTemplate;

  private final String employeeId = "EMP123456";

  @BeforeEach
  void setUp() {
    Member member =
        Member.builder()
            .employeeIdentificationNumber(employeeId)
            .employeeName("김테스트")
            .email("example@example.com")
            .phoneNumber("01011111111")
            .password(passwordEncoder.encode("password123"))
            .role(MemberRole.ADMIN)
            .build();

    memberRepository.save(member);

    // 유효한 리프레시 토큰 발급 및 Redis 저장
    String refreshToken = jwtTokenProvider.createRefreshToken(employeeId, member.getRole().name());
    RefreshToken redisToken = RefreshToken.builder().token(refreshToken).build();

    redisTemplate.opsForValue().set(employeeId, redisToken, Duration.ofDays(7));
  }

  @AfterEach
  void cleanUp() {
    redisTemplate.delete(employeeId);
  }

  @Test
  void login_success() throws Exception {
    LoginRequest loginRequest = new LoginRequest(employeeId, "password123");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andReturn();

    String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).contains("refreshToken=");
  }

  @Test
  void login_userNotFound() throws Exception {
    LoginRequest loginRequest = new LoginRequest("NOT_FOUND", "password123");

    mockMvc
        .perform(
            post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
  }

  @Test
  void login_invalidPassword() throws Exception {
    LoginRequest loginRequest = new LoginRequest(employeeId, "wrongPassword");

    mockMvc
        .perform(
            post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."));
  }

  @Test
  void refreshToken_success() throws Exception {
    // given
    String validRefreshToken = redisTemplate.opsForValue().get(employeeId).getToken();

    RefreshTokenRequest request = new RefreshTokenRequest(validRefreshToken);

    // when
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/members/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andReturn();

    // then
    String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).contains("refreshToken=");

    // refreshToken=...; 로부터 실제 토큰 문자열만 파싱
    String cookieRefreshToken =
        Arrays.stream(setCookieHeader.split(";"))
            .filter(part -> part.trim().startsWith("refreshToken="))
            .map(part -> part.trim().substring("refreshToken=".length()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("refreshToken not found in Set-Cookie header"));

    // 쿠키에 포함된 리프레시 토큰이 응답 객체(또는 Redis 저장값)과 일치하는지 검증
    assertThat(cookieRefreshToken).isNotBlank();
    assertThat(cookieRefreshToken)
        .isEqualTo(redisTemplate.opsForValue().get(employeeId).getToken());
  }

  @Test
  void refreshToken_user_does_not_exist() throws Exception {
    // given
    String fakeRefreshToken = jwtTokenProvider.createRefreshToken("FAKE_USER", "USER");
    RefreshTokenRequest request = new RefreshTokenRequest(fakeRefreshToken);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/members/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void refreshToken_token_not_found_in_redis() throws Exception {
    // given
    String notStoredToken =
        jwtTokenProvider.createRefreshToken(employeeId, "USER"); // 새로운 토큰이기 때문에 redis에 없음
    RefreshTokenRequest request = new RefreshTokenRequest(notStoredToken);

    redisTemplate.delete(employeeId); // Redis에 저장된 기존 토큰 제거

    // when & then
    mockMvc
        .perform(
            post("/api/v1/members/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is4xxClientError());
  }
}
