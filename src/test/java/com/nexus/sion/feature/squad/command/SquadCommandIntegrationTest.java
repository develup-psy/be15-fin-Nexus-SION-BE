package com.nexus.sion.feature.squad.command;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.aggregate.Project.ProjectStatus;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.command.repository.DomainRepository;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;

@SpringBootTest
@AutoConfigureMockMvc
class SquadCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private DomainRepository domainRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private SquadCommandRepository squadCommandRepository;

  @BeforeEach
  void setup() {
    domainRepository.save(Domain.of("CS"));
    // 프로젝트 더미 데이터 삽입
    Project project =
        Project.builder()
            .projectCode("PRJ001")
            .clientCode("C001")
            .title("더미 프로젝트")
            .description("이 프로젝트는 테스트용입니다.")
            .startDate(LocalDate.of(2025, 1, 1))
            .expectedEndDate(LocalDate.of(2025, 12, 31))
                .budget(10_000_000L)
                .status(ProjectStatus.WAITING)
            .requestSpecificationUrl("http://example.com/spec")
            .domainName("CS")
            .build();
    projectRepository.save(project);

    // 스쿼드 더미 데이터 삽입
    Squad squad =
        Squad.builder()
            .squadCode("SQUAD001")
            .projectCode("PRJ001")
            .title("기존 스쿼드")
            .description("기존 설명")
            .originType(OriginType.MANUAL)
            .build();
    squadCommandRepository.save(squad);
  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 등록 성공")
  void registerSquad_success() throws Exception {
    SquadRegisterRequest request =
        SquadRegisterRequest.builder()
            .projectCode("PRJ001")
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

    mockMvc
        .perform(
            post("/api/v1/squads/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 등록 실패 - 필수 필드 누락")
  void registerSquad_fail_whenMissingRequiredField() throws Exception {
    SquadRegisterRequest request =
        SquadRegisterRequest.builder()
            .projectCode("PRJ001")
            .title(null) // 필수 누락
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
    SquadUpdateRequest request =
        SquadUpdateRequest.builder()
            .squadCode("SQUAD001")
            .projectCode("PRJ001")
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
            .projectCode("PRJ001")
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

  @WithMockUser
  @Test
  @DisplayName("스쿼드 삭제 성공")
  void deleteSquad_success() throws Exception {
    // given
    String squadCode = "SQUAD001";

    // when & then
    mockMvc
        .perform(delete("/api/v1/squads/{squadCode}", squadCode))
        .andExpect(status().isNoContent());
  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 삭제 실패 - 존재하지 않는 스쿼드 코드")
  void deleteSquad_fail_whenSquadNotFound() throws Exception {
    mockMvc
        .perform(delete("/api/v1/squads/{squadCode}", "invalid_code"))
        .andExpect(status().isNotFound());
  }
}
