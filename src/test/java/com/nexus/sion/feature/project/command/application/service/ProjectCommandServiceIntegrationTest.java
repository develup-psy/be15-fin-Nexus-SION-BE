package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.JobInfo;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.TechStackInfo;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.repository.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ProjectCommandServiceIntegrationTest {

    @Autowired
    private ProjectCommandService projectCommandService;

    @Autowired
    private ProjectCommandRepository projectCommandRepository;

    @Test
    void 프로젝트_정상_등록_통합테스트() {
        // given
        ProjectRegisterRequest request = createRequest();

        // when
        ProjectRegisterResponse response = projectCommandService.registerProject(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.projectCode()).isEqualTo("P123");
        assertThat(projectCommandRepository.existsByProjectCode("P123")).isTrue();
    }

    @Test
    void 프로젝트_중복_등록_통합테스트() {
        // given
        ProjectRegisterRequest request = createRequest();
        projectCommandService.registerProject(request);

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
