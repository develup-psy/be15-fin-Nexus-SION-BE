package com.nexus.sion.feature.statistics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class StatisticsQueryIntergrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("POST /stack/member-count - 스택별 인원 수 조회")
  void getStackCount() throws Exception {
    List<String> stacks = List.of("Java", "React");

    mockMvc
        .perform(
            post("/api/v1/statistics/stack/member-count")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stacks)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @DisplayName("GET /all-tech-stacks - 모든 기술스택 조회")
  void getAllTechStacks() throws Exception {
    mockMvc
        .perform(get("/api/v1/statistics/all-tech-stacks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  @DisplayName("GET /developers - 전체 개발자 목록 조회")
  void getAllDevelopers() throws Exception {
    mockMvc
        .perform(get("/api/v1/statistics/developers").param("page", "1").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray());
  }

  @Test
  @DisplayName("GET /stack/average-career - 기술스택별 평균 경력 조회")
  void getStackAverageCareerPaged() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/statistics/stack/average-career")
                .param("selectedStacks", "Java", "Spring")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "techStackName")
                .param("direction", "asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray());
  }
}
