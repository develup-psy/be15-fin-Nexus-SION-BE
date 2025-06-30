package com.nexus.sion.feature.project.command.application.service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.nexus.sion.feature.project.command.application.dto.response.FPInferResponse;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectAnalysisResult;
import com.nexus.sion.feature.project.command.domain.service.ProjectDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.domain.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectCommandServiceImpl implements ProjectCommandService {

  private final ProjectCommandRepository projectCommandRepository;
  private final ProjectAndJobRepository projectAndJobRepository;
  private final JobAndTechStackRepository jobAndTechStackRepository;
  private final RestTemplate restTemplate;
  private final ProjectDomainService projectDomainService;
  private final ProjectFunctionEstimateRepository projectFunctionEstimateRepository;
  private final ProjectFpSummaryRepository projectFpSummaryRepository;

  @Override
  public ProjectRegisterResponse registerProject(ProjectRegisterRequest request) {
    if (projectCommandRepository.existsByProjectCode(request.getProjectCode())) {
      throw new BusinessException(ErrorCode.PROJECT_CODE_DUPLICATED);
    }

    Project project =
            Project.builder()
                    .projectCode(request.getProjectCode())
                    .domainName(request.getDomainName())
                    .description(request.getDescription())
                    .title(request.getTitle())
                    .budget(request.getBudget())
                    .startDate(request.getStartDate())
                    .expectedEndDate(request.getExpectedEndDate())
                    .status(Project.ProjectStatus.WAITING)
                    .numberOfMembers(request.getNumberOfMembers())
                    .clientCode(request.getClientCode())
                    .requestSpecificationUrl(request.getRequestSpecificationUrl())
                    .build();
    projectCommandRepository.save(project);

    saveJobsAndTechStacks(request);
    return new ProjectRegisterResponse(request.getProjectCode());
  }

  @Override
  public void updateProject(ProjectRegisterRequest request) {
    Project project =
            projectCommandRepository
                    .findById(request.getProjectCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    project.setDomainName(request.getDomainName());
    project.setDescription(request.getDescription());
    project.setTitle(request.getTitle());
    project.setBudget(request.getBudget());
    project.setStartDate(request.getStartDate());
    project.setExpectedEndDate(request.getExpectedEndDate());
    project.setNumberOfMembers(request.getNumberOfMembers());
    project.setClientCode(request.getClientCode());
    project.setRequestSpecificationUrl(request.getRequestSpecificationUrl());
    projectCommandRepository.save(project);

    var projectAndJobs = projectAndJobRepository.findByProjectCode(request.getProjectCode());
    projectAndJobs.forEach(
            job -> {
              jobAndTechStackRepository.deleteByProjectJobId(job.getId());
            });
    projectAndJobRepository.deleteByProjectCode(request.getProjectCode());

    saveJobsAndTechStacks(request);
  }

  private void saveJobsAndTechStacks(ProjectRegisterRequest request) {
    request
            .getJobs()
            .forEach(
                    job -> {
                      ProjectAndJob projectAndJob =
                              ProjectAndJob.builder()
                                      .projectCode(request.getProjectCode())
                                      .jobName(job.getJobName())
                                      .requiredNumber(job.getRequiredNumber())
                                      .build();
                      projectAndJobRepository.save(projectAndJob);

                      job.getTechStacks()
                              .forEach(
                                      tech -> {
                                        JobAndTechStack jobAndTechStack =
                                                JobAndTechStack.builder()
                                                        .projectJobId(projectAndJob.getId())
                                                        .techStackName(tech.getTechStackName())
                                                        .priority(tech.getPriority())
                                                        .build();
                                        jobAndTechStackRepository.save(jobAndTechStack);
                                      });
                    });
  }

  @Override
  public void deleteProject(String projectCode) {
    Project project =
            projectCommandRepository
                    .findById(projectCode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    var projectAndJobs = projectAndJobRepository.findByProjectCode(projectCode);
    projectAndJobs.forEach(
            job -> {
              jobAndTechStackRepository.deleteByProjectJobId(job.getId());
            });
    projectAndJobRepository.deleteByProjectCode(projectCode);

    projectCommandRepository.delete(project);
  }

  @Override
  public void updateProjectStatus(String projectCode, Project.ProjectStatus status) {
    Project project =
            projectCommandRepository
                    .findById(projectCode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    project.setStatus(status);
    if (status == Project.ProjectStatus.COMPLETE) {
      project.setActualEndDate(LocalDate.now());
    } else {
      project.setActualEndDate(null);
    }
    projectCommandRepository.save(project);
  }

  @Override
  public Map<String, Long> findProjectAndJobIdMap(String projectId) {
    return projectAndJobRepository.findByProjectCode(projectId).stream()
            .collect(Collectors.toMap(
                    ProjectAndJob::getJobName,
                    ProjectAndJob::getId
            ));
  }

  @Transactional
  @Override
  @Async
  public void analyzeProject(String projectId, MultipartFile multipartFile) {
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

      ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8100/fp-infer", request, String.class);

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

      List<ProjectFunctionEstimate> updatedFunctions = analysisResult.functions().stream()
              .filter(func -> {
                boolean hasName = StringUtils.hasText(func.getFunctionName());
                if (!hasName) {
                  log.warn("FP 분석 결과에 function_name이 없는 항목이 있어 저장에서 제외됩니다. 내용: {}", func);
                }
                return hasName;
              })
              .map(func -> ProjectFunctionEstimate.builder()
                      .projectFpSummaryId(savedSummary.getId())
                      .functionName(func.getFunctionName())
                      .functionType(func.getFunctionType())
                      .complexity(func.getComplexity())
                      .functionScore(func.getFunctionScore())
                      .description(func.getDescription())
                      .relatedTablesCount(func.getRelatedTablesCount())
                      .relatedFieldsCount(func.getRelatedFieldsCount())
                      .build()
              )
              .toList();

      projectFunctionEstimateRepository.saveAll(updatedFunctions);

    } catch (Exception e) {
      log.error("[FP 분석 실패] {}", e.getMessage(), e);
      throw new RuntimeException(e);  // 반드시 rethrow
    } finally {
      if (tempFile != null) tempFile.delete();
    }
  }
}
