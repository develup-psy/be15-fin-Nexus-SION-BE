package com.nexus.sion.feature.project.query;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectAndJob;
import com.nexus.sion.feature.project.query.dto.request.ReplacementRecommendationRequest;
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

import java.time.LocalDate;

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

  @Test
  @DisplayName("스쿼드용 프로젝트 상세 조회 성공")
  void getProjectInfoForSquad_success() throws Exception {
    String existingProjectCode = "ha_1_1";

    mockMvc
            .perform(
                    get("/api/v1/projects/squad/{projectCode}", existingProjectCode)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.projectCode", not(emptyOrNullString())))
            .andExpect(jsonPath("$.data.jobRequirements").isArray())
            .andExpect(jsonPath("$.data.estimatedDuration").exists())
            .andExpect(jsonPath("$.data.estimatedCost").exists())
            .andExpect(jsonPath("$.data.budgetLimit").exists())
            .andExpect(jsonPath("$.data.durationLimit").exists())
            .andExpect(jsonPath("$.data.totalEffort").exists());
  }

  @Test
  @DisplayName("프로젝트 인원 대체 추천 성공")
  void recommendReplacement_success() throws Exception {
    ReplacementRecommendationRequest request = ReplacementRecommendationRequest.builder()
            .projectCode("ha_1_1")
            .leavingMember("EMP004")
            .build();

    mockMvc.perform(post("/api/v1/projects/replacement")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id", not(emptyOrNullString())))
            .andExpect(jsonPath("$.data[0].name", not(emptyOrNullString())))
            .andExpect(jsonPath("$.data[0].grade", not(emptyOrNullString())))
            .andExpect(jsonPath("$.data[0].avgTechScore").exists())
            .andExpect(jsonPath("$.data[0].domainCount").exists())
            .andExpect(jsonPath("$.data[0].monthlyUnitPrice").exists())
            .andExpect(jsonPath("$.data[0].productivity").exists());
  }
}
