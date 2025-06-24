package com.nexus.sion.feature.project.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.JobInfo;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest.TechStackInfo;
import com.nexus.sion.feature.project.command.domain.repository.ProjectCommandRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectCommandServiceIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ProjectCommandRepository projectCommandRepository;

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
    assertThat(projectCommandRepository.existsByProjectCode(request.getProjectCode())).isTrue();
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
    request.setName("Updated Name");
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
    assertThat(projectCommandRepository.existsByProjectCode(request.getProjectCode())).isFalse();
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
