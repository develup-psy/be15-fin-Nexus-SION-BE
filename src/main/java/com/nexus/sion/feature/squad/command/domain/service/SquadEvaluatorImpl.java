package com.nexus.sion.feature.squad.command.domain.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import com.nexus.sion.feature.project.command.domain.repository.ProjectFpSummaryRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.command.application.dto.internal.CandidateSummary;
import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SquadEvaluatorImpl {

  private final ProjectFpSummaryRepository projectFpSummaryRepository;
  private final ProjectRepository projectRepository;

  public List<EvaluatedSquad> evaluateAll(
      String projectId, List<Map<String, List<CandidateSummary>>> squadCombinations) {

    // 프로젝트 정보 조회를 위해
    Project project =
        projectRepository
            .findByProjectCode(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    Long maxBudget = project.getBudget(); // null 가능
    Integer maxDuration = null;
    if (project.getExpectedEndDate() != null && project.getStartDate() != null) {
      long days = ChronoUnit.DAYS.between(project.getStartDate(), project.getExpectedEndDate());
      maxDuration = (int) Math.ceil(days / 30.0);
    }

    // 총 FP 조회
    int totalFP =
        projectFpSummaryRepository
            .findByProjectCode(projectId)
            .map(ProjectFpSummary::getTotalFp)
            .orElseThrow(() -> new BusinessException(ErrorCode.FP_NOT_FOUND));

    double effortRate = getEffortRatePerFP(totalFP);
    int totalManMonth = (int) Math.ceil(totalFP * effortRate);
    List<EvaluatedSquad> results = new ArrayList<>();

    for (Map<String, List<CandidateSummary>> squadMap : squadCombinations) {
      List<CandidateSummary> members = squadMap.values().stream().flatMap(List::stream).toList();

      if (members.isEmpty()) continue;

      int techSum = members.stream().mapToInt(CandidateSummary::getTechStackScore).sum();
      double domainSum = members.stream().mapToDouble(CandidateSummary::getDomainRelevance).sum();
      int monthlyCost = members.stream().mapToInt(CandidateSummary::getCostPerMonth).sum();
      double productivitySum =
          members.stream().mapToDouble(CandidateSummary::getProductivityFactor).sum();

      if (productivitySum == 0) continue;

      double estimatedDuration = Math.ceil(totalManMonth / productivitySum);
      int totalCost = (int) Math.ceil(monthlyCost * estimatedDuration);

      // 예산/기간 조건 필터링
      //      boolean overBudget = maxBudget != null && totalCost > maxBudget;
      //      boolean overDuration = maxDuration != null && estimatedDuration > maxDuration;
      //
      //      if (overBudget || overDuration) continue;
      System.out.println("maxBudget = " + maxBudget);
      System.out.println("maxDuration = " + maxDuration);

      String reason = "";
      if (Objects.nonNull(maxBudget) && Objects.nonNull(maxDuration)) {
        reason = "예산과 기간 조건을 만족하는 최적 조합입니다.";
      } else if (Objects.nonNull(maxBudget)) {
        reason = "예산을 만족하는 최적 조합입니다.";
      } else if (Objects.nonNull(maxDuration)) {
        reason = "기간을 만족하는 최적 조합입니다. ";
      }

      results.add(
          new EvaluatedSquad(
              squadMap,
              techSum / members.size(),
              domainSum / members.size(),
              monthlyCost,
              totalManMonth,
              (int) estimatedDuration,
              totalCost,
              reason));
    }

    return results;
  }

  private static double getEffortRatePerFP(int totalFp) {
    if (totalFp <= 100) return 0.15;
    else if (totalFp <= 300) return 0.125;
    else if (totalFp <= 600) return 0.10;
    else return 0.08;
  }
}
