package com.nexus.sion.feature.member.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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
import com.nexus.sion.feature.member.command.application.dto.request.*;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("회원 가입 - 성공")
  void register_success() throws Exception {
    MemberCreateRequest request =
        MemberCreateRequest.builder()
            .employeeIdentificationNumber("DEV001")
            .employeeName("홍길동")
            .email("hong@example.com")
            .password("Test@1234")
            .phoneNumber("01012345678")
            .birthday(String.valueOf(LocalDate.of(1990, 1, 1)))
            .build();

    mockMvc
        .perform(
            post("/api/v1/members/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true));

    assertThat(memberRepository.existsByEmail("hong@example.com")).isTrue();
  }

  @Test
  @DisplayName("개발자 등록 - 성공")
  @WithMockUser
  void addMembers_success() throws Exception {
    MemberAddRequest request =
        new MemberAddRequest(
            "DEV002",
            "이순신",
            "01011112222",
            LocalDate.of(1991, 2, 3),
            LocalDate.of(2020, 1, 1),
            "lee@example.com",
            4,
            "사원",
            "개발팀",
            "https://cdn.example.com/img.jpg",
            5000L,
            List.of("Java", "Spring"));

    mockMvc
        .perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("정보 수정 - 성공")
  @WithMockUser
  void updateMember_success() throws Exception {
    Member member = createDummyMember("DEV003");
    memberRepository.save(member);

    MemberUpdateRequest request =
        new MemberUpdateRequest(
            "홍길동",
            "01088887777",
            LocalDate.of(1990, 5, 5),
            LocalDate.of(2020, 1, 1),
            "update@example.com",
            5,
            "대리",
            "개발팀",
            "https://cdn.com/img.jpg",
            6500L,
            List.of("React", "Java"));

    mockMvc
        .perform(
            put("/api/v1/members/{employeeId}", "DEV003")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    Member updated = memberRepository.findById("DEV003").orElseThrow();
    assertThat(updated.getEmail()).isEqualTo("update@example.com");
    assertThat(updated.getEmployeeName()).isEqualTo("홍길동");
  }

  @Test
  @DisplayName("삭제 - 성공")
  @WithMockUser
  void deleteMember_success() throws Exception {
    Member member = createDummyMember("DEV004");
    memberRepository.save(member);

    mockMvc
        .perform(delete("/api/v1/members/{employeeId}", "DEV004"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    Member deleted = memberRepository.findById("DEV004").orElseThrow();
    assertThat(deleted.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("상태 변경 - 성공")
  void updateStatus_success() throws Exception {
    Member member = createDummyMember("DEV005");
    memberRepository.save(member);

    MemberStatusUpdateRequest request = new MemberStatusUpdateRequest(MemberStatus.UNAVAILABLE);

    mockMvc
        .perform(
            patch("/api/v1/members/{employeeId}/status", "DEV005")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    assertThat(memberRepository.findById("DEV005").orElseThrow().getStatus())
        .isEqualTo(MemberStatus.UNAVAILABLE);
  }

  private Member createDummyMember(String id) {
    return Member.builder()
        .employeeIdentificationNumber(id)
        .employeeName("더미")
        .email(id.toLowerCase() + "@test.com")
        .phoneNumber("01099999999")
        .birthday(LocalDate.of(1990, 1, 1))
        .joinedAt(LocalDate.of(2020, 1, 1))
        .careerYears(2)
        .role(MemberRole.INSIDER)
        .password("encoded")
        .build();
  }
}
