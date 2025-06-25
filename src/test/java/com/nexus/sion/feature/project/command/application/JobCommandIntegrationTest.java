package com.nexus.sion.feature.project.command.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.project.command.application.dto.request.JobRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Job;
import com.nexus.sion.feature.project.command.repository.JobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JobCommandIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
        int existingCount = jobRepository.findAll().size();

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

}
