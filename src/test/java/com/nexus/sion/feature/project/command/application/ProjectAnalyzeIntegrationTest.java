package com.nexus.sion.feature.project.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.common.fastapi.FastApiClient;
import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.aggregate.Project.ProjectStatus;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFunctionEstimate;
import com.nexus.sion.feature.project.command.domain.repository.ProjectFpSummaryRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectFunctionEstimateRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;

@SpringBootTest(
    properties = {
      "ai.fp-infer=http://localhost:8000/fp-infer",
      "ai.embed-function=http://localhost:8000/embed",
      "ai.fp-freelencer-infer=http://localhost:8000/freelancer-fp-infer"
    })
@AutoConfigureMockMvc
@Import({
  ProjectAnalyzeIntegrationTest.TestAsyncConfig.class,
  ProjectAnalyzeIntegrationTest.MockFastApiConfig.class
})
class ProjectAnalyzeIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Autowired private ProjectRepository projectRepository;
  @Autowired private ProjectFpSummaryRepository projectFpSummaryRepository;
  @Autowired private ProjectFunctionEstimateRepository projectFunctionEstimateRepository;
  @Autowired private ClientCompanyRepository clientCompanyRepository;

  @Autowired private FastApiClient fastApiClient;

  private final String testProjectCode = "ka_3_1";

  @BeforeEach
  void setUp() {

    // 1. 선행 데이터 - 클라이언트 회사 저장
    clientCompanyRepository.save(
        ClientCompany.builder().clientCode("ka_3").companyName("카카오택시").domainName("CS").build());

    projectRepository.save(
        Project.builder()
            .projectCode(testProjectCode)
            .clientCode("ka_3")
            .title("테스트 프로젝트")
            .description("통합 테스트용")
            .startDate(LocalDate.of(2025, 1, 1))
            .expectedEndDate(LocalDate.of(2025, 12, 31))
            .numberOfMembers(1)
            .budget(10_000_000L)
            .status(ProjectStatus.WAITING)
            .requestSpecificationUrl("http://example.com/spec")
            .domainName("CS")
            .build());
  }

  @Test
  @WithMockUser
  @DisplayName("프로젝트 분석 성공 → DB 결과 확인")
  void analyzeProject_success() throws Exception {
    // given
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "requirement.pdf", "application/pdf", "dummy content".getBytes());

    String responseJson =
        """
            {
              "project_id": "%s",
              "total_fp_score" : 160,
              "functions": [
                {
                  "function_name": "로그인",
                  "description": "사용자 로그인 기능",
                  "fp_type": "EI",
                  "complexity": "SIMPLE",
                  "score": 3,
                  "estimated_ftr": 1,
                  "estimated_det": 2
                },
                {
                  "function_name": "회원가입",
                  "description": "신규 사용자 등록",
                  "fp_type": "EO",
                  "complexity": "COMPLEX",
                  "score": 7,
                  "estimated_ftr": 3,
                  "estimated_det": 6
                }
              ]
            }
        """
            .formatted(testProjectCode);

    ResponseEntity<String> fastApiResponse = ResponseEntity.ok(responseJson);

    given(fastApiClient.requestFpInference(anyString(), any(File.class)))
        .willReturn(fastApiResponse);

    // when
    mockMvc
        .perform(multipart("/api/v1/projects/{projectCode}/analyze", testProjectCode).file(file))
        .andExpect(status().isAccepted());

    // then
    Project updated = projectRepository.findById(testProjectCode).orElseThrow();
    assertThat(updated.getAnalysisStatus()).isEqualTo(Project.AnalysisStatus.PROCEEDING);

    ProjectFpSummary summary =
        projectFpSummaryRepository.findByProjectCode(testProjectCode).orElseThrow();
    assertThat(summary.getTotalFp()).isEqualTo(160);

    List<ProjectFunctionEstimate> functions = projectFunctionEstimateRepository.findAll();
    assertThat(functions).hasSize(2);
    assertThat(functions)
        .extracting(ProjectFunctionEstimate::getFunctionName)
        .containsExactlyInAnyOrder("로그인", "회원가입");
  }

  @TestConfiguration
  static class TestAsyncConfig {
    @Bean(name = "testTaskExecutor") // 이름 충돌 방지
    public Executor taskExecutor() {
      return Runnable::run;
    }
  }

  @TestConfiguration
  static class MockFastApiConfig {
    @Bean
    public FastApiClient fastApiClient() {
      return Mockito.mock(FastApiClient.class);
    }
  }
}
