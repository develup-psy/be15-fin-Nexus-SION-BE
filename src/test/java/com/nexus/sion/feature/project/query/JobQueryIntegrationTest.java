package com.nexus.sion.feature.project.query;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.feature.project.command.domain.aggregate.Job;
import com.nexus.sion.feature.project.command.repository.JobRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JobQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JobRepository jobRepository;

  @Test
  void 직무_전체조회_성공() throws Exception {
    // 테스트 데이터 저장
    String jobName = "test";

    jobRepository.save(Job.of(jobName));
    jobRepository.flush();

    // when & then
    mockMvc
        .perform(get("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.jobs", hasItems(jobName)));
  }

  @Test
  void 직무_정렬_알파벳순_성공() throws Exception {
    // given
    jobRepository.save(Job.of("Banana"));
    jobRepository.save(Job.of("apple"));
    jobRepository.save(Job.of("디자이너"));
    jobRepository.save(Job.of("가나다"));
    jobRepository.save(Job.of("개발자"));
    jobRepository.flush();

    // when & then
    mockMvc
        .perform(get("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.jobs[0]").value("apple"))
        .andExpect(jsonPath("$.data.jobs[1]").value("Banana"))
        .andExpect(jsonPath("$.data.jobs[2]").value("가나다"))
        .andExpect(jsonPath("$.data.jobs[3]").value("개발자"))
        .andExpect(jsonPath("$.data.jobs[4]").value("디자이너"));
  }
}
