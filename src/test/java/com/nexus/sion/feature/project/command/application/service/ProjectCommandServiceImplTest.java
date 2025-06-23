package com.nexus.sion.feature.project.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
  void 프로젝트_정상_등록() {
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
  void 프로젝트_코드_중복_등록_실패() {
    // given
    ProjectRegisterRequest request = createRequest();

    when(projectCommandRepository.existsByProjectCode(request.getProjectCode())).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> projectCommandService.registerProject(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.PROJECT_CODE_DUPLICATED.getMessage());
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
    request.setName("프로젝트 A");
    request.setDescription("프로젝트 설명");
    request.setTitle("프로젝트 제목");
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
