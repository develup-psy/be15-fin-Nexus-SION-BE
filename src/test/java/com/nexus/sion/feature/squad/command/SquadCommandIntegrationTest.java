package com.nexus.sion.feature.squad.command;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
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
  @Autowired private ClientCompanyRepository clientCompanyRepository;
  @BeforeEach
  void setup() {
    // 1. 선행 데이터 - 클라이언트 회사 저장
    clientCompanyRepository.save(
            ClientCompany.builder()
                    .clientCode("C001")
                    .companyName("카카오")
                    .domainName("CS")
                    .build());

    // 2. 도메인 저장
    domainRepository.save(Domain.of("CS"));

    // 3. 프로젝트 저장
    Project project =
            Project.builder()
                    .projectCode("PRJ001")
                    .clientCode("C001") // 위에서 저장한 client_code 사용
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

    // 4. 스쿼드 저장
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
            .projectCode("ha_1_1")
            .title("스쿼드 A")
            .description("신규 백엔드 개발 스쿼드")
            .members(
                List.of(
                    SquadRegisterRequest.Member.builder()
                        .employeeIdentificationNumber("01202305")
                        .projectAndJobId(1L)
                        .build(),
                    SquadRegisterRequest.Member.builder()
                        .employeeIdentificationNumber("02202306")
                        .projectAndJobId(2L)
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
                        .employeeIdentificationNumber("01202305")
                        .projectAndJobId(1L)
                        .build(),
                    SquadUpdateRequest.Member.builder()
                        .employeeIdentificationNumber("02202306")
                        .projectAndJobId(3L)
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
                        .employeeIdentificationNumber("01202305")
                        .projectAndJobId(1L)
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
