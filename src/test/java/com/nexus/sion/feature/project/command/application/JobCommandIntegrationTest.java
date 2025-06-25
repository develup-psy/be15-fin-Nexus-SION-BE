package com.nexus.sion.feature.project.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.JobRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Job;
import com.nexus.sion.feature.project.command.repository.JobRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JobCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JobRepository jobRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("새로운 직무를 등록하면 201이 반환된다.")
  void registerNewJob_returnsCreated() throws Exception {
    // given
    String jobName = "test";
    JobRequest request = JobRequest.builder().name(jobName).build();

    // when & then
    mockMvc
        .perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated()); // 201 반환되는지 확인

    // then - DB에 저장되었는지 확인
    assertThat(jobRepository.findById(jobName)).isPresent();
  }

  @Test
  @DisplayName("이미 존재하는 직무는 저장하지 않고, 200이 반환된다.")
  void registerExistingJob_doesNotSaveAgain() throws Exception {
    // given
    String existingJobName = "jobName";
    jobRepository.save(Job.of(existingJobName));
    long existingCount = jobRepository.count();

    JobRequest request = JobRequest.builder().name(existingJobName).build();

    // when & then
    mockMvc
        .perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk()); // 200 반환되는지 확인

    // then - 여전히 하나만 존재
    assertThat(jobRepository.findAll().size()).isEqualTo(existingCount);
  }

  @Test
  @DisplayName("직무를 삭제하면 204가 반환되고 DB에서 제거된다.")
  void deleteExistingJob_returnsDeleted() throws Exception {
    // given
    String jobName = "test";
    jobRepository.save(Job.of(jobName));

    // when & then
    mockMvc.perform(delete("/api/v1/jobs/{jobName}", jobName)).andExpect(status().isNoContent());

    // then: DB에서 해당 기술 스택이 제거되었는지 확인한다.
    assertThat(jobRepository.findById(jobName)).isNotPresent();
  }

  @Test
  @DisplayName("존재하지 않는 직무는 에러를 반환한다.")
  void deleteNonExistingJob_returnsError() throws Exception {
    // given
    String jobName = "test";

    // when & then
    mockMvc
        .perform(delete("/api/v1/jobs/{jobName}", jobName))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.JOB_NOT_FOUND.getCode()))
        .andExpect(jsonPath("$.message").value(ErrorCode.JOB_NOT_FOUND.getMessage()))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
