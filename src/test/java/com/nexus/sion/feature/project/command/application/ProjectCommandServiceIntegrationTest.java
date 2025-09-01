package com.nexus.sion.feature.project.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.JobInfo;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.TechStackInfo;
import com.nexus.sion.feature.project.command.application.dto.request.SquadReplacementRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.domain.aggregate.JobAndTechStack;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectAndJob;
import com.nexus.sion.feature.project.command.domain.repository.JobAndTechStackRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectAndJobRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectCommandServiceIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private SquadCommandRepository squadCommandRepository;
  @Autowired private SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ClientCompanyRepository clientCompanyRepository;
  @Autowired private ProjectAndJobRepository projectAndJobRepository;
  @Autowired private JobAndTechStackRepository jobAndTechStackRepository;

  @Test
  @DisplayName("프로젝트 등록 성공")
  void registerProject_success() throws Exception {
    // given
    ProjectRegisterRequest request = createRequest();

    // when & then
    mockMvc
        .perform(
            post("/api/v1/projects") // 👉 실제 Controller URL 맞게 수정
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.errorCode").doesNotExist());

    // DB 저장 검증
    assertThat(projectRepository.existsByProjectCode(request.getProjectCode())).isTrue();
  }

  @Test
  @DisplayName("프로젝트 수정 성공")
  void updateProject_success() throws Exception {
    ProjectRegisterRequest request = createRequest();

    // 등록 먼저 수행
    mockMvc
        .perform(
            post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // 수정
    request.setTitle("Updated Title");
    request.setDescription("Updated Description");

    mockMvc
        .perform(
            put("/api/v1/projects/{projectCode}", request.getProjectCode())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("프로젝트 삭제 성공")
  void deleteProject_success() throws Exception {
    ProjectRegisterRequest request = createRequest();

    // 등록 먼저 수행
    mockMvc
        .perform(
            post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // 삭제
    mockMvc
        .perform(
            delete("/api/v1/projects/{projectCode}", request.getProjectCode())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // DB 검증 (삭제되었는지 확인)
    assertThat(projectRepository.existsByProjectCode(request.getProjectCode())).isFalse();
  }

  @Test
  @DisplayName("프로젝트 상태 변경 성공")
  void updateProjectStatus_success() throws Exception {
    // 등록 먼저 수행
    ProjectRegisterRequest request = createRequest();
    mockMvc
        .perform(
            post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // 상태 변경
    mockMvc
        .perform(
            put(
                "/api/v1/projects/{projectCode}/status/{status}",
                request.getProjectCode(),
                "COMPLETE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  private ProjectRegisterRequest createRequest() {
    TechStackInfo techStack = new TechStackInfo();
    techStack.setTechStackName("Java");
    techStack.setPriority(1);

    JobInfo job = new JobInfo();
    job.setJobName("back");
    job.setRequiredNumber(2);
    job.setTechStacks(List.of(techStack));

    ProjectRegisterRequest request = new ProjectRegisterRequest();
    request.setProjectCode("P123");
    request.setDomainName("testdomain");
    request.setDescription("설명");
    request.setTitle("제목");
    request.setBudget(new BigDecimal(1000000L));
    request.setStartDate(LocalDate.now());
    request.setExpectedEndDate(LocalDate.now().plusDays(30));
    request.setClientCode("CLIENT123");
    request.setNumberOfMembers(5);
    request.setRequestSpecificationUrl("https://s3.url/spec.pdf");
    request.setJobs(List.of(job));

    return request;
  }

  @Test
  @DisplayName("스쿼드 구성원 대체 성공")
  void replaceSquadMember_success() throws Exception {

    String squadCode = "ka_2_1_1";
    String oldEmployeeId = "12345678";
    String newEmployeeId = "87654321";
    String projectCode = "ka_2_1";

    clientCompanyRepository.save(
        ClientCompany.builder().clientCode("ka_2").companyName("카카오페이").domainName("CS").build());

    projectRepository.save(
        Project.builder()
            .projectCode(projectCode)
            .clientCode("ka_2")
            .title("테스트 프로젝트")
            .description("통합 테스트용")
            .startDate(LocalDate.of(2025, 1, 1))
            .expectedEndDate(LocalDate.of(2025, 12, 31))
            .numberOfMembers(1)
            .budget(new BigDecimal(10_000_000))
            .status(Project.ProjectStatus.WAITING)
            .requestSpecificationUrl("http://example.com/spec")
            .domainName("CS")
            .build());

    squadCommandRepository.save(
        Squad.builder()
            .squadCode(squadCode)
            .projectCode("ka_2_1")
            .title("기존 스쿼드")
            .description("기존 설명")
            .estimatedCost(BigDecimal.valueOf(10_000_000L))
            .estimatedDuration(BigDecimal.valueOf(12L))
            .originType(OriginType.MANUAL)
            .build());

    memberRepository.save(
        Member.builder()
            .employeeIdentificationNumber(oldEmployeeId)
            .employeeName("홍길동")
            .password("encoded-password")
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

    memberRepository.save(
        Member.builder()
            .employeeIdentificationNumber(newEmployeeId)
            .employeeName("이순신")
            .password("encoded-password")
            .phoneNumber("01098765432")
            .positionName("사원")
            .departmentName("개발팀")
            .birthday(LocalDate.of(1994, 2, 2))
            .joinedAt(LocalDate.of(2021, 2, 1))
            .email("lee@example.com")
            .careerYears(2)
            .salary(40000000L)
            .status(MemberStatus.AVAILABLE)
            .gradeCode(GradeCode.C)
            .role(MemberRole.INSIDER)
            .build());

    ProjectAndJob job =
        projectAndJobRepository.save(
            ProjectAndJob.builder()
                .projectCode(projectCode)
                .jobName("백엔드")
                .requiredNumber(1)
                .build());

    jobAndTechStackRepository.save(
        JobAndTechStack.builder()
            .projectJobId(job.getId())
            .techStackName("Java")
            .priority(1)
            .build());

    SquadEmployee oldMember =
        SquadEmployee.builder()
            .squadCode(squadCode)
            .employeeIdentificationNumber(oldEmployeeId)
            .projectAndJobId(job.getId())
            .isLeader(false)
            .build();
    squadEmployeeCommandRepository.save(oldMember);

    SquadReplacementRequest request =
        SquadReplacementRequest.builder()
            .squadCode(squadCode)
            .oldEmployeeId(oldEmployeeId)
            .newEmployeeId(newEmployeeId)
            .build();

    // when & then
    mockMvc
        .perform(
            put("/api/v1/projects/squad/replacement")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    boolean oldMemberExists =
        squadEmployeeCommandRepository.existsBySquadCodeAndEmployeeIdentificationNumber(
            squadCode, oldEmployeeId);
    boolean newMemberExists =
        squadEmployeeCommandRepository.existsBySquadCodeAndEmployeeIdentificationNumber(
            squadCode, newEmployeeId);

    assertThat(oldMemberExists).isFalse();
    assertThat(newMemberExists).isTrue();
  }
}
