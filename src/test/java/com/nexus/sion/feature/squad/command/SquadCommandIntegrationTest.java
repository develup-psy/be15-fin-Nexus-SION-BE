package com.nexus.sion.feature.squad.command;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
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

  @WithMockUser
  @Test
  @DisplayName("스쿼드 수정 성공")
  void updateSquad_success() throws Exception {
    // given (먼저 등록된 스쿼드가 DB에 있어야 합니다)
    SquadUpdateRequest request =
            SquadUpdateRequest.builder()
                    .squadCode("ha_1_1_1") // DB에 존재하는 스쿼드 코드로 바꿔야 함
                    .projectCode("ha_1_1")
                    .title("수정된 스쿼드 제목")
                    .description("수정된 설명입니다.")
                    .members(
                            List.of(
                                    SquadUpdateRequest.Member.builder()
                                            .employeeIdentificationNumber("EMP001")
                                            .projectAndJobId(101L)
                                            .build(),
                                    SquadUpdateRequest.Member.builder()
                                            .employeeIdentificationNumber("EMP003")
                                            .projectAndJobId(103L)
                                            .build()))
                    .build();

    // when & then
    mockMvc
            .perform(
                    put("/api/v1/squads/manual")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 수정 실패 - 존재하지 않는 스쿼드 코드")
  void updateSquad_fail_whenSquadNotFound() throws Exception {
    SquadUpdateRequest request =
            SquadUpdateRequest.builder()
                    .squadCode("not_exist_code")
                    .projectCode("ha_1_1")
                    .title("제목")
                    .description("설명")
                    .members(
                            List.of(
                                    SquadUpdateRequest.Member.builder()
                                            .employeeIdentificationNumber("EMP001")
                                            .projectAndJobId(101L)
                                            .build()))
                    .build();

    mockMvc
            .perform(
                    put("/api/v1/squads/manual")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
  }
}
