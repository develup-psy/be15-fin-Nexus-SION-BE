package com.nexus.sion.feature.squad.command;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.domain.repository.JobAndTechStackRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectAndJobRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectFpSummaryRepository;
import com.nexus.sion.feature.squad.command.application.dto.request.Developer;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.service.SquadCommandService;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
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
import com.nexus.sion.feature.project.command.domain.aggregate.Project.ProjectStatus;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
import com.nexus.sion.feature.project.command.repository.DomainRepository;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SquadCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private DomainRepository domainRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private SquadCommandRepository squadCommandRepository;
  @Autowired private ClientCompanyRepository clientCompanyRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ProjectAndJobRepository projectAndJobRepository;
  @Autowired private JobAndTechStackRepository jobAndTechStackRepository;
  @Autowired
  private ProjectFpSummaryRepository projectFpSummaryRepository;

  private Long validProjectAndJobId;

  @BeforeEach
  void setup() {
    // 1. 선행 데이터 - 클라이언트 회사 저장
    clientCompanyRepository.save(
        ClientCompany.builder().clientCode("ka_2").companyName("카카오페이").domainName("CS").build());

    // 2. 도메인 저장
    domainRepository.save(Domain.of("CS"));

    // 3. 프로젝트 저장

    projectRepository.save(
            Project.builder()
                    .projectCode("ka_2_1")
                    .clientCode("ka_2")
                    .title("테스트 프로젝트")
                    .description("통합 테스트용")
                    .startDate(LocalDate.of(2025, 1, 1))
                    .expectedEndDate(LocalDate.of(2025, 12, 31))
                    .numberOfMembers(1)
                    .budget(10_000_000L)
                    .status(ProjectStatus.WAITING)
                    .requestSpecificationUrl("http://example.com/spec")
                    .domainName("CS")
                    .build());
    // 4. 스쿼드 저장
    Squad squad =
        Squad.builder()
            .squadCode("ka_2_1_1")
            .projectCode("ka_2_1")
            .title("기존 스쿼드")
            .description("기존 설명")
            .estimatedCost(BigDecimal.valueOf(10_000_000L))
            .estimatedDuration(BigDecimal.valueOf(12L))
            .originType(OriginType.MANUAL)
            .build();
    squadCommandRepository.save(squad);

    // 멤버 저장
    memberRepository.save(
            Member.builder()
                    .employeeIdentificationNumber("9999999")
                    .employeeName("홍길동")
                    .password("encoded-password")
                    .profileImageUrl(null)
                    .phoneNumber("01012345678")
                    .positionName("과장")
                    .departmentName("개발팀")
                    .birthday(LocalDate.of(1995, 5, 5))
                    .joinedAt(LocalDate.of(2020, 1, 1))
                    .email("hong@example.com")
                    .careerYears(3)
                    .salary(50000000L)
                    .status(MemberStatus.AVAILABLE)
                    .gradeCode(GradeCode.B)
                    .role(MemberRole.INSIDER)
                    .build());

    // project_and_job 저장
    ProjectAndJob job1 = projectAndJobRepository.save(
            ProjectAndJob.builder()
                    .projectCode("ka_2_1")
                    .jobName("백엔드")
                    .requiredNumber(1)
                    .build());

    validProjectAndJobId = job1.getId();
    System.out.println("[validProjectAndJobId 인 셋업] = " + validProjectAndJobId);


    //job_and_tech 저장
    jobAndTechStackRepository.save(
            JobAndTechStack.builder()
                    .projectJobId(validProjectAndJobId)
                    .techStackName("Java")
                    .priority(1)
                    .build());

  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 등록 성공")
  void registerSquad_success() throws Exception {
    BigDecimal estimatedCost = BigDecimal.valueOf(10_000_000L);
    BigDecimal estimatedDuration = BigDecimal.valueOf(12L);
    SquadRegisterRequest request =
        SquadRegisterRequest.builder()
            .projectCode("ka_2_1")
            .title("스쿼드 A")
            .description("신규 백엔드 개발 스쿼드")
            .estimatedCost(estimatedCost)
            .estimatedDuration(estimatedDuration)
            .developers(
                List.of(
                    Developer.builder()
                        .employeeId("9999999")
                        .projectAndJobId(validProjectAndJobId)
                        .build()))
            .build();

    mockMvc
        .perform(
            post("/api/v1/squads/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("스쿼드 A"));
  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 등록 실패 - 필수 필드 누락")
  void registerSquad_fail_whenMissingRequiredField() throws Exception {
    BigDecimal estimatedCost = BigDecimal.valueOf(10_000_000L);
    BigDecimal estimatedDuration = BigDecimal.valueOf(12L);
    SquadRegisterRequest request =
            SquadRegisterRequest.builder()
                    .projectCode("ka_2_1")
                    .title(null) //필수 필드 누락
                    .description("신규 백엔드 개발 스쿼드")
                    .estimatedCost(estimatedCost)
                    .estimatedDuration(estimatedDuration)
                    .developers(
                            List.of(
                                    Developer.builder()
                                            .employeeId("9999999")
                                            .projectAndJobId(validProjectAndJobId)
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
            .squadCode("ka_2_1_1")
            .title("수정된 스쿼드 제목")
            .description("수정된 설명입니다.")
                .estimatedCost(BigDecimal.valueOf(1_000_000L))
                .estimatedDuration(BigDecimal.valueOf(11L))
            .developers(
                List.of(
                    Developer.builder()
                        .employeeId("9999999")
                        .projectAndJobId(validProjectAndJobId)
                        .build()))
            .build();

    mockMvc
        .perform(
            put("/api/v1/squads/manual")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("수정된 스쿼드 제목"));
  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 수정 실패 - 존재하지 않는 스쿼드 코드")
  void updateSquad_fail_whenSquadNotFound() throws Exception {
    SquadUpdateRequest request =
            SquadUpdateRequest.builder()
                    .squadCode("없는 스쿼드 코드")
                    .title("수정된 스쿼드 제목")
                    .description("수정된 설명입니다.")
                    .estimatedCost(BigDecimal.valueOf(1_000_000L))
                    .estimatedDuration(BigDecimal.valueOf(11L))
                    .developers(
                            List.of(
                                    Developer.builder()
                                            .employeeId("9999999")
                                            .projectAndJobId(validProjectAndJobId)
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
  @DisplayName("스쿼드 수정 실패 -  상한선을 넘는 스쿼드")
  void updateSquad_fail_whenSquadExceedThreshold() throws Exception {
    SquadUpdateRequest request =
            SquadUpdateRequest.builder()
                    .squadCode("ka_2_1_1")
                    .title("수정된 스쿼드 제목")
                    .description("수정된 설명입니다.")
                    .estimatedCost(BigDecimal.valueOf(1_500_000L))
                    .estimatedDuration(BigDecimal.valueOf(13L))
                    .developers(
                            List.of(
                                    Developer.builder()
                                            .employeeId("9999999")
                                            .projectAndJobId(validProjectAndJobId)
                                            .build()))
                    .build();

    mockMvc
            .perform(
                    put("/api/v1/squads/manual")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
  }

  @WithMockUser
  @Test
  @DisplayName("스쿼드 삭제 성공")
  void deleteSquad_success() throws Exception {
    // given
    String squadCode = "ka_2_1_1";

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

  @WithMockUser
  @Test
  @DisplayName("스쿼드 추천 성공")
  void recommendSquad_success() throws Exception {
    // 프로젝트 분석 정보 저장
    projectFpSummaryRepository.save(
            ProjectFpSummary.builder()
                    .projectCode("ka_2_1")
                    .totalFp(160)
                    .avgEffortPerFp(20)
                    .totalEffort(BigDecimal.valueOf(32))
                    .estimatedDuration(BigDecimal.valueOf(5.0))
                    .estimatedCost(BigDecimal.valueOf(5_000_000.0))
                    .build()
    );

    SquadRecommendationRequest request =
            SquadRecommendationRequest.builder()
                    .projectId("ka_2_1")
                    .criteria(RecommendationCriteria.BALANCED)
                    .build();
    mockMvc
            .perform(
                    post("/api/v1/squads/recommendation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.squadCode").exists());

  }


}
