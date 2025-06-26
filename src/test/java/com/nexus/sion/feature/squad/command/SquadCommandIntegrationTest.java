package com.nexus.sion.feature.squad.command;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;

@SpringBootTest
@AutoConfigureMockMvc
class SquadCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @WithMockUser
  @Test
  @DisplayName("스쿼드 등록 성공")
  void registerSquad_success() throws Exception {
    // given
    SquadRegisterRequest request =
        SquadRegisterRequest.builder()
            .projectCode("ha_1_1")
            .title("스쿼드 A")
            .description("신규 백엔드 개발 스쿼드")
            .members(
                List.of(
                    SquadRegisterRequest.Member.builder()
                        .employeeIdentificationNumber("EMP001")
                        .projectAndJobId(101L)
                        .build(),
                    SquadRegisterRequest.Member.builder()
                        .employeeIdentificationNumber("EMP002")
                        .projectAndJobId(102L)
                        .build()))
            .build();

    // when & then
    mockMvc
        .perform(
            post("/api/v1/squads/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 등록 실패 - 필수 필드 누락 시 400 반환")
  void registerSquad_fail_whenMissingRequiredField() throws Exception {
    SquadRegisterRequest request =
        SquadRegisterRequest.builder()
            .projectCode("ha_1_1")
            .title(null) // 필수값 누락
            .description("설명 없음")
            .members(
                List.of(
                    SquadRegisterRequest.Member.builder()
                        .employeeIdentificationNumber("EMP001")
                        .projectAndJobId(101L)
                        .build()))
            .build();

    mockMvc
        .perform(
            post("/api/v1/squads/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
