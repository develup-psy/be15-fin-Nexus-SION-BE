package com.nexus.sion.feature.project.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.application.dto.request.SquadReplacementRequest;
import com.nexus.sion.feature.project.command.domain.service.ProjectAnalysisService;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.JobInfo;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.TechStackInfo;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectUpdateRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.domain.repository.*;
import org.springframework.web.multipart.MultipartFile;

class ProjectCommandServiceImplTest {

  @Mock private ProjectCommandRepository projectCommandRepository;
  @Mock private ProjectAndJobRepository projectAndJobRepository;
  @Mock private JobAndTechStackRepository jobAndTechStackRepository;
  @Mock private SquadCommandRepository squadCommandRepository;
  @Mock private SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private ProjectFpSummaryRepository projectFpSummaryRepository;
  @Mock private ProjectFunctionEstimateRepository projectFunctionEstimateRepository;
  @Mock private ProjectAnalysisService projectAnalysisService;
  @Mock private ProjectRepository projectRepository;

  @Mock private SquadEmployeeCommandRepository squadEmployeeCommandRepository;

  @InjectMocks private ProjectCommandServiceImpl projectCommandService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("프로젝트 등록 성공")
  void registerProject_Success() {
    // given
    ProjectRegisterRequest request = createRequest();
    when(projectRepository.findProjectCodesByClientCode(request.getClientCode()))
            .thenReturn(List.of()); // ✅ 코드 생성 위한 mock

    // when
    ProjectRegisterResponse response = projectCommandService.registerProject(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.projectCode()).startsWith(request.getClientCode());
    verify(projectCommandRepository).save(any(Project.class));
    verify(projectAndJobRepository, atLeastOnce()).save(any(ProjectAndJob.class));
    verify(jobAndTechStackRepository, atLeastOnce()).save(any(JobAndTechStack.class));
  }

  @Test
  @DisplayName("프로젝트 수정 성공 - 기본 정보만 수정")
  void updateProject_Success() {
    // given
    ProjectUpdateRequest request = createUpdateRequest();

    Project existingProject = Project.builder().projectCode(request.getProjectCode()).build();

    when(projectCommandRepository.findById(request.getProjectCode()))
        .thenReturn(Optional.of(existingProject));

    // when
    projectCommandService.updateProject(request);

    // then
    verify(projectCommandRepository).save(existingProject);
    verifyNoInteractions(jobAndTechStackRepository); // 기술스택 저장/삭제 없음
    verifyNoInteractions(projectAndJobRepository); // 직무 변경도 없음
  }

  @Test
  @DisplayName("프로젝트 수정 실패 - 존재하지 않는 프로젝트")
  void updateProject_Fail_When_ProjectNotFound() {
    // given
    ProjectUpdateRequest request = createUpdateRequest();
    when(projectCommandRepository.findById(request.getProjectCode())).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> projectCommandService.updateProject(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.PROJECT_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("프로젝트 삭제 성공")
  void deleteProject_Success() {
    // given
    String projectCode = "P123";
    Project existingProject = Project.builder().projectCode(projectCode).build();

    when(projectCommandRepository.findById(projectCode)).thenReturn(Optional.of(existingProject));
    when(projectAndJobRepository.findByProjectCode(projectCode))
        .thenReturn(List.of(ProjectAndJob.builder().id(1L).build()));

    // when
    projectCommandService.deleteProject(projectCode);

    // then
    verify(jobAndTechStackRepository).deleteByProjectJobId(anyLong());
    verify(projectAndJobRepository).deleteByProjectCode(projectCode);
    verify(projectCommandRepository).delete(existingProject);
  }

  @Test
  @DisplayName("프로젝트 삭제 실패 - 존재하지 않는 프로젝트")
  void deleteProject_Fail_When_ProjectNotFound() {
    // given
    String projectCode = "P123";
    when(projectCommandRepository.findById(projectCode)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> projectCommandService.deleteProject(projectCode))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.PROJECT_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("프로젝트 상태 변경 성공")
  void updateProjectStatus_Success() {
    // given
    String projectCode = "P123";
    Project project =
            Project.builder().projectCode(projectCode).status(Project.ProjectStatus.WAITING).build();

    when(projectCommandRepository.findById(projectCode)).thenReturn(Optional.of(project));
    when(squadEmployeeCommandRepository.findByProjectCode(projectCode))
            .thenReturn(List.of()); // ✅ 상태 변경 후 작업 요청 처리를 위한 mock

    // when
    projectCommandService.updateProjectStatus(projectCode, Project.ProjectStatus.COMPLETE);

    // then
    assertThat(project.getStatus()).isEqualTo(Project.ProjectStatus.COMPLETE);
    assertThat(project.getActualEndDate()).isNotNull();
    verify(projectCommandRepository).save(project);
  }


  @Test
  @DisplayName("프로젝트 상태 변경 실패 - 존재하지 않는 프로젝트")
  void updateProjectStatus_Fail_NotFound() {
    // given
    String projectCode = "NOT_EXIST";
    when(projectCommandRepository.findById(projectCode)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () ->
                projectCommandService.updateProjectStatus(
                    projectCode, Project.ProjectStatus.IN_PROGRESS))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.PROJECT_NOT_FOUND.getMessage());
  }

  private ProjectRegisterRequest createRequest() {
    TechStackInfo techStack = new TechStackInfo();
    techStack.setTechStackName("Java");
    techStack.setPriority(1);

    JobInfo job = new JobInfo();
    job.setJobName("Backend");
    job.setRequiredNumber(2);
    job.setTechStacks(List.of(techStack));

    ProjectRegisterRequest request = new ProjectRegisterRequest();
    request.setProjectCode("P123");
    request.setDomainName("Project A");
    request.setDescription("Project Description");
    request.setTitle("Project Title");
    request.setBudget(1000000L);
    request.setStartDate(LocalDate.now());
    request.setExpectedEndDate(LocalDate.now().plusDays(30));
    request.setClientCode("CLIENT1");
    request.setNumberOfMembers(5);
    request.setRequestSpecificationUrl("https://s3.url/spec.pdf");
    request.setJobs(List.of(job));
    return request;
  }

  private ProjectUpdateRequest createUpdateRequest() {
    return ProjectUpdateRequest.builder()
        .projectCode("P123")
        .domainName("Updated Domain")
        .description("Updated Description")
        .title("Updated Title")
        .budget(2000000L)
        .startDate(LocalDate.now())
        .expectedEndDate(LocalDate.now().plusDays(60))
        .numberOfMembers(4)
        .requestSpecificationUrl("https://s3.url/updated_spec.pdf")
        .build(); // ✅ jobTechStacks 제거됨
  }

  @Test
  @DisplayName("프로젝트 인원 교체 성공")
  void replaceMember_success() {
    // given
    String squadCode = "SQD001";
    String oldEmployeeId = "OLD001";
    String newEmployeeId = "NEW001";

    Squad squad = Squad.builder()
            .squadCode(squadCode)
            .projectCode("P123")
            .build();

    SquadEmployee oldMember = SquadEmployee.builder()
            .squadCode(squadCode)
            .employeeIdentificationNumber(oldEmployeeId)
            .projectAndJobId(1001L)
            .isLeader(false)
            .build();

    Member newMember = Member.builder()
            .employeeIdentificationNumber(newEmployeeId)
            .status(MemberStatus.AVAILABLE)
            .build();

    given(squadCommandRepository.findById(squadCode)).willReturn(Optional.of(squad));
    given(squadEmployeeCommandRepository.findBySquadCodeAndEmployeeIdentificationNumber(squadCode, oldEmployeeId))
            .willReturn(Optional.of(oldMember));
    given(squadEmployeeCommandRepository.existsBySquadCodeAndEmployeeIdentificationNumber(squadCode, newEmployeeId))
            .willReturn(false);
    given(memberRepository.findById(newEmployeeId)).willReturn(Optional.of(newMember));

    SquadReplacementRequest request = SquadReplacementRequest.builder()
            .squadCode(squadCode)
            .oldEmployeeId(oldEmployeeId)
            .newEmployeeId(newEmployeeId)
            .build();

    // when
    projectCommandService.replaceMember(request);

    // then
    verify(squadEmployeeCommandRepository).deleteBySquadCodeAndEmployeeIdentificationNumber(squadCode, oldEmployeeId);
    verify(squadEmployeeCommandRepository).save(any(SquadEmployee.class));
  }

  @Test
  @DisplayName("인원 교체 실패 - 스쿼드 없음")
  void replaceMember_fail_squadNotFound() {
    String squadCode = "INVALID";
    SquadReplacementRequest request = SquadReplacementRequest.builder()
            .squadCode(squadCode)
            .oldEmployeeId("OLD")
            .newEmployeeId("NEW")
            .build();

    given(squadCommandRepository.findById(squadCode)).willReturn(Optional.empty());

    assertThatThrownBy(() -> projectCommandService.replaceMember(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.SQUAD_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("인원 교체 실패 - 리더 교체 불가")
  void replaceMember_fail_invalidLeaderReplacement() {
    String squadCode = "SQD001";
    Squad squad = Squad.builder().squadCode(squadCode).build();
    SquadEmployee leader = SquadEmployee.builder().isLeader(true).build();

    given(squadCommandRepository.findById(squadCode)).willReturn(Optional.of(squad));
    given(squadEmployeeCommandRepository.findBySquadCodeAndEmployeeIdentificationNumber(any(), any()))
            .willReturn(Optional.of(leader));

    SquadReplacementRequest request = SquadReplacementRequest.builder()
            .squadCode(squadCode)
            .oldEmployeeId("OLD")
            .newEmployeeId("NEW")
            .build();

    assertThatThrownBy(() -> projectCommandService.replaceMember(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.INVALID_LEADER_REPLACEMENT.getMessage());
  }

  @Test
  @DisplayName("프로젝트 FP 분석 성공")
  void analyzeProject_success() {
    // given
    String projectId = "P123";
    String employeeId = "EMP001";
    MultipartFile mockFile = mock(MultipartFile.class);

    Project project = Project.builder()
            .projectCode(projectId)
            .status(Project.ProjectStatus.WAITING)
            .analysisStatus(Project.AnalysisStatus.PENDING)
            .build();

    ProjectFpSummary fpSummary = ProjectFpSummary.builder()
            .id(1L)
            .projectCode(projectId)
            .build();

    given(projectFpSummaryRepository.findByProjectCode(projectId)).willReturn(Optional.of(fpSummary));
    doNothing().when(projectFunctionEstimateRepository).deleteByProjectFpSummaryId(fpSummary.getId());
    doNothing().when(projectFpSummaryRepository).deleteByProjectCode(projectId);
    given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
    given(projectAnalysisService.analyzeProject(eq(projectId), eq(mockFile), eq(employeeId)))
            .willReturn(CompletableFuture.completedFuture(null));

    // when
    projectCommandService.analyzeProject(projectId, mockFile, employeeId);

    // then
    assertThat(project.getAnalysisStatus()).isEqualTo(Project.AnalysisStatus.PROCEEDING);
    verify(projectFunctionEstimateRepository).deleteByProjectFpSummaryId(fpSummary.getId());
    verify(projectFpSummaryRepository).deleteByProjectCode(projectId);
    verify(projectAnalysisService).analyzeProject(projectId, mockFile, employeeId);
    verify(projectRepository).save(project);
  }

  @Test
  @DisplayName("프로젝트 FP 분석 실패 - 프로젝트 없음")
  void analyzeProject_fail_projectNotFound() {
    // given
    String projectId = "INVALID";
    MultipartFile mockFile = mock(MultipartFile.class);

    given(projectFpSummaryRepository.findByProjectCode(projectId)).willReturn(Optional.empty());
    given(projectRepository.findById(projectId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> projectCommandService.analyzeProject(projectId, mockFile, "EMP001"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.PROJECT_NOT_FOUND.getMessage());
  }

}
