package com.nexus.sion.feature.member.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.member.command.application.dto.request.MemberAddRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.domain.repository.DepartmentRepository;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.member.command.domain.repository.PositionRepository;
import com.nexus.sion.feature.member.command.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 끝나면 자동 롤백됨!
class MemberCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private MemberRepository memberRepository;

  @Autowired private PositionRepository positionRepository;

  @Autowired private DepartmentRepository departmentRepository;

  @Autowired private DeveloperTechStackRepository developerTechStackRepository;

  @Test
  @DisplayName("회원 가입 성공")
  void register_success() throws Exception {
    // given
    MemberCreateRequest request =
        MemberCreateRequest.builder()
            .employeeIdentificationNumber("EMP12345")
            .employeeName("홍길동")
            .email("example@example.com")
            .password("Test@1234")
            .phoneNumber("01012345678")
            .build();

    // when & then
    mockMvc
        .perform(
            post("/api/v1/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").doesNotExist())
        .andExpect(jsonPath("$.data").doesNotExist());

    // DB 저장 검증
    assertThat(memberRepository.existsByEmail(request.getEmail())).isTrue();
  }

  @Test
  @DisplayName("개발자 다건 등록 - 성공")
  @WithMockUser
  void addMembers_success() throws Exception {
    MemberAddRequest request =
        new MemberAddRequest(
            "EMP999",
            "홍길동",
            "01012345678",
            LocalDate.of(1990, 1, 1),
            LocalDateTime.of(2022, 1, 1, 9, 0),
            "hong999@example.com",
            3,
            "Backend",
            "UX",
            "https://cdn.example.com/profile.jpg",
            5000L,
            List.of("JAVA"));

    String json = objectMapper.writeValueAsString(List.of(request));

    mockMvc
        .perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}
