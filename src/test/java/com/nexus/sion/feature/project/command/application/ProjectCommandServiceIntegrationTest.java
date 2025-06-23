package com.nexus.sion.feature.project.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.JobInfo;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.TechStackInfo;
import com.nexus.sion.feature.project.command.domain.repository.ProjectCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectCommandServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectCommandRepository projectCommandRepository;

    @Test
    @DisplayName("프로젝트 등록 성공")
    void registerProject_success() throws Exception {
        // given
        ProjectRegisterRequest request = createRequest();

        // when & then
        mockMvc.perform(post("/api/v1/projects") // 👉 실제 Controller URL 맞게 수정
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.errorCode").doesNotExist());

        // DB 저장 검증
        assertThat(projectCommandRepository.existsByProjectCode(request.getProjectCode())).isTrue();
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
        request.setName("testdomain");
        request.setDescription("설명");
        request.setTitle("제목");
        request.setBudget(1000000L);
        request.setStartDate(LocalDate.now());
        request.setExpectedEndDate(LocalDate.now().plusDays(30));
        request.setClientCode("CLIENT123");
        request.setNumberOfMembers(5);
        request.setRequestSpecificationUrl("https://s3.url/spec.pdf");
        request.setJobs(List.of(job));

        return request;
    }
}
