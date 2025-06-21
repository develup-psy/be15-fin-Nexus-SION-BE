package com.nexus.sion.feature.auth.command;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.auth.command.application.dto.request.LoginRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트가 끝나면 DB를 원래 상태로 되돌린다.
public class AuthControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private MemberRepository memberRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    Member member =
        Member.builder()
            .employeeIdentificationNumber("EMP001")
            .password(passwordEncoder.encode("password123"))
            .role(MemberRole.ADMIN)
            .build();

    memberRepository.save(member);
  }

  @Test
  void login_success() throws Exception {
    LoginRequest loginRequest = new LoginRequest("EMP001", "password123");

    mockMvc
        .perform(
            post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andExpect(jsonPath("$.data.refreshToken").exists());
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
    LoginRequest loginRequest = new LoginRequest("EMP001", "wrongPassword");

    mockMvc
        .perform(
            post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."));
  }
}
