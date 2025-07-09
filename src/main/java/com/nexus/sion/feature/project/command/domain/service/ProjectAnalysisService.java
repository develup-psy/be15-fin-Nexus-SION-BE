package com.nexus.sion.feature.project.command.domain.service;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.response.FPInferResponse;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectAnalysisResult;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFunctionEstimate;
import com.nexus.sion.feature.project.command.domain.repository.ProjectFpSummaryRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectFunctionEstimateRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProjectAnalysisService {

  private final RestTemplate restTemplate;
  private final ProjectDomainService projectDomainService;
  private final ProjectFpSummaryRepository projectFpSummaryRepository;
  private final ProjectFunctionEstimateRepository projectFunctionEstimateRepository;
  private final ProjectRepository projectRepository;
  private final NotificationCommandService notificationCommandService;

  @Value("${ai.fp-infer-url}")
  private String fpInferUrl;

  @Async
  public CompletableFuture<Void> analyzeProject(String projectId, MultipartFile multipartFile, String managerId) {
    File tempFile = null;
    try {
      tempFile = File.createTempFile("input_", ".pdf");
      multipartFile.transferTo(tempFile);

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("project_id", projectId);
      body.add("file", new FileSystemResource(tempFile));

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

      ResponseEntity<String> response =
          restTemplate.postForEntity(fpInferUrl, request, String.class);

      log.info(response.getBody());

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new IllegalStateException("FP 분석 실패: " + response.getStatusCode());
      }

      ObjectMapper mapper = new ObjectMapper();
      mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
      FPInferResponse result = mapper.readValue(response.getBody(), FPInferResponse.class);
      log.info("FPInferResponse:{}", result);

      ProjectAnalysisResult analysisResult = projectDomainService.analyze(result);
      log.info("ProjectAnalysisResult.functions:{}", analysisResult.functions());
      log.info("ProjectAnalysisResult.summary:{}", analysisResult.summary());

      ProjectFpSummary savedSummary = projectFpSummaryRepository.save(analysisResult.summary());
      log.info("[Saved]!! ProjectFpSummary:{}", savedSummary);

      List<ProjectFunctionEstimate> updatedFunctions =
          analysisResult.functions().stream()
              .filter(
                  func -> {
                    boolean hasName = StringUtils.hasText(func.getFunctionName());
                    if (!hasName) {
                      log.warn("FP 분석 결과에 function_name이 없는 항목이 있어 저장에서 제외됩니다. 내용: {}", func);
                    }
                    return hasName;
                  })
              .map(
                  func ->
                      ProjectFunctionEstimate.builder()
                          .projectFpSummaryId(savedSummary.getId())
                          .functionName(func.getFunctionName())
                          .functionType(func.getFunctionType())
                          .complexity(func.getComplexity())
                          .functionScore(func.getFunctionScore())
                          .description(func.getDescription())
                          .relatedTablesCount(func.getRelatedTablesCount())
                          .relatedFieldsCount(func.getRelatedFieldsCount())
                          .build())
              .toList();

      projectFunctionEstimateRepository.saveAll(updatedFunctions);

      Project project =
          projectRepository
              .findById(projectId)
              .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
      project.setAnalysisStatus(Project.AnalysisStatus.COMPLETE);
      Project savedProject = projectRepository.save(project);

      notifyFPAnalysisSuccess(managerId, projectId);

      return CompletableFuture.completedFuture(null);

    } catch (Exception e) {
      log.error("[FP 분석 실패] {}", e.getMessage(), e);
      throw new RuntimeException(e);
    } finally {
      if (tempFile != null && tempFile.exists()) {
        boolean deleted = tempFile.delete();
        if (!deleted) {
          log.warn("임시 파일 삭제 실패: {}", tempFile.getAbsolutePath());
        }
      }
    }
  }

  private void notifyFPAnalysisSuccess(String managerId, String projectId) {

    notificationCommandService.createAndSendNotification(
            null,
            managerId,
            NotificationType.FP_ANALYSIS_COMPLETE,
            projectId
    );
  }
}
