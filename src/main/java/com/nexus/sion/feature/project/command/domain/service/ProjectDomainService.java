package com.nexus.sion.feature.project.command.domain.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.command.application.dto.response.FPInferResponse;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectAnalysisResult;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFunctionEstimate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProjectDomainService {

  public ProjectAnalysisResult analyze(FPInferResponse result) {
    String projectCode = result.getProjectId();
    int totalFp = result.getTotalFpScore();

    // ⏱️ 예상 기간/예산 계산
    double effortPerFP = 0.25;
    double totalEffort = totalFp * effortPerFP;
    double estimatedDuration = Math.ceil(totalEffort / 2.0);
    int costPerManMonth = 5_000_000;
    int estimatedCost = (int) (totalEffort * costPerManMonth);

    // project_fp_summary 객체 생성
    ProjectFpSummary summary =
        ProjectFpSummary.builder()
            .projectCode(projectCode)
            .totalFp(totalFp)
            .avgEffortPerFp((int) (effortPerFP * 100))
            .totalEffort(BigDecimal.valueOf(totalEffort))
            .estimatedDuration(BigDecimal.valueOf(estimatedDuration))
            .estimatedCost(BigDecimal.valueOf(estimatedCost))
            .build();

    log.info("ProjectFpSummary:{}", summary);

    // 기능 단위 리스트 생성
    List<ProjectFunctionEstimate> functions =
        result.getFunctions().stream()
            .filter(
                func ->
                    isValidFunctionType(func.getFpType())
                        && isValidComplexity(func.getComplexity()))
            .map(
                func ->
                    ProjectFunctionEstimate.builder()
                        .projectFpSummaryId(summary.getId())
                        .functionType(
                            ProjectFunctionEstimate.FunctionType.valueOf(
                                func.getFpType().toUpperCase()))
                        .complexity(
                            ProjectFunctionEstimate.Complexity.valueOf(
                                func.getComplexity().toUpperCase()))
                        .functionScore(func.getScore())
                        .description(func.getDescription())
                        .relatedTablesCount(func.getEstimatedDet())
                        .relatedFieldsCount(func.getEstimatedFtr())
                        .functionName(func.getFunctionName())
                        .build())
            .toList();

    log.info("functions:{}", functions);

    return new ProjectAnalysisResult(summary, functions);
  }

  private boolean isValidFunctionType(String value) {
    if (value == null) return false;
    try {
      ProjectFunctionEstimate.FunctionType.valueOf(value.toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private boolean isValidComplexity(String value) {
    if (value == null) return false;
    try {
      ProjectFunctionEstimate.Complexity.valueOf(value.toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
