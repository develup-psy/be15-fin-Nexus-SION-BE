package com.nexus.sion.feature.project.query;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private ProjectListRequest defaultRequest;

  @BeforeEach
  void setUp() {
    defaultRequest = new ProjectListRequest();
    defaultRequest.setPage(0);
    defaultRequest.setSize(10);
  }

  @Test
  @DisplayName("프로젝트 목록 조회 성공")
  void findProjects_success() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/projects/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(defaultRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content", is(not(empty()))));
  }

  @Test
  @DisplayName("프로젝트 상세 조회 성공")
  void getProjectDetail_success() throws Exception {
    String existingProjectCode = "PRJ001";

    mockMvc
        .perform(
            get("/api/v1/projects/list/{projectCode}", existingProjectCode)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.title", not(emptyOrNullString())))
        .andExpect(jsonPath("$.data.domainName", not(emptyOrNullString())))
        .andExpect(jsonPath("$.data.duration", containsString("~")))
        .andExpect(jsonPath("$.data.budget", not(emptyOrNullString())))
        .andExpect(jsonPath("$.data.techStacks", is(not(empty()))))
        .andExpect(jsonPath("$.data.members").isArray());
  }
}
