package com.nexus.sion.feature.project.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.domain.repository.*;

class ProjectCommandServiceImplTest {

  @Mock private ProjectCommandRepository projectCommandRepository;
  @Mock private ProjectAndJobRepository projectAndJobRepository;
  @Mock private JobAndTechStackRepository jobAndTechStackRepository;

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

    when(projectCommandRepository.existsByProjectCode(request.getProjectCode())).thenReturn(false);

    // when
    ProjectRegisterResponse response = projectCommandService.registerProject(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.projectCode()).isEqualTo("P123");
    verify(projectCommandRepository).save(any(Project.class));
    verify(projectAndJobRepository, atLeastOnce()).save(any(ProjectAndJob.class));
    verify(jobAndTechStackRepository, atLeastOnce()).save(any(JobAndTechStack.class));
  }

  @Test
  @DisplayName("중복 프로젝트 코드 등록 실패")
  void registerProject_Fail_When_Duplicated_ProjectCode() {
    // given
    ProjectRegisterRequest request = createRequest();

    when(projectCommandRepository.existsByProjectCode(request.getProjectCode())).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> projectCommandService.registerProject(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.PROJECT_CODE_DUPLICATED.getMessage());
  }

  @Test
  @DisplayName("프로젝트 수정 성공")
  void updateProject_Success() {
    // given
    ProjectRegisterRequest request = createRequest();

    Project existingProject = Project.builder().projectCode(request.getProjectCode()).build();

    when(projectCommandRepository.findById(request.getProjectCode()))
        .thenReturn(Optional.of(existingProject));
    when(projectAndJobRepository.findByProjectCode(request.getProjectCode()))
        .thenReturn(List.of(ProjectAndJob.builder().id(1L).build()));

    // when
    projectCommandService.updateProject(request);

    // then
    verify(projectCommandRepository).save(existingProject);
    verify(jobAndTechStackRepository).deleteByProjectJobId(anyLong());
    verify(projectAndJobRepository).deleteByProjectCode(request.getProjectCode());
  }

  @Test
  @DisplayName("없는 프로젝트 수정 시 예외 발생")
  void updateProject_Fail_When_ProjectNotFound() {
    // given
    ProjectRegisterRequest request = createRequest();
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
}
