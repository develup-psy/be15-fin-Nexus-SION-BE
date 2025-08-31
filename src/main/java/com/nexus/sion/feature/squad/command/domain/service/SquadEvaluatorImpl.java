package com.nexus.sion.feature.squad.command.domain.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nexus.sion.feature.project.query.dto.internal.ProjectEvaluationInfo;
import com.nexus.sion.feature.project.query.service.ProjectEvaluationService;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
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

  private final ProjectEvaluationService projectEvaluationService;

  public List<EvaluatedSquad> evaluateAll(
      String projectId, List<Map<String, List<DeveloperSummary>>>  squadCombinations) {

    ProjectEvaluationInfo info = projectEvaluationService.getEvaluationInfo(projectId);
    BigDecimal maxBudget = info.maxBudget();
    Integer maxDuration = info.maxDuration();

    double effortRate = getEffortRatePerFP(info.totalFP());

    int totalManMonth = (int) Math.ceil(info.totalFP() * effortRate);
    List<EvaluatedSquad> results = new ArrayList<>();

    for (Map<String, List<DeveloperSummary>> squadMap : squadCombinations) {
      List<DeveloperSummary> members = squadMap.values().stream().flatMap(List::stream).toList();

      if (members.isEmpty()) continue;

      double techSum = members.stream().mapToDouble(DeveloperSummary::getAvgTechScore).sum();
      double domainSum = members.stream().mapToDouble(DeveloperSummary::getDomainCount).sum();
      int monthlyCost = members.stream().mapToInt(DeveloperSummary::getMonthlyUnitPrice).sum();
      double productivitySum =
          members.stream().mapToDouble(DeveloperSummary::getProductivity).sum();

      if (productivitySum == 0) continue;

      double estimatedDuration = Math.ceil(totalManMonth / productivitySum);
      BigDecimal totalCost = new BigDecimal(Math.ceil(monthlyCost * estimatedDuration));

      boolean overBudget = maxBudget != null && totalCost.compareTo(maxBudget) > 0;
      boolean overDuration = maxDuration != null && estimatedDuration > maxDuration;

      if (overBudget || overDuration) continue;
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

  public static double getEffortRatePerFP(int totalFp) {
    if (totalFp <= 100) return 0.15;
    else if (totalFp <= 300) return 0.125;
    else if (totalFp <= 600) return 0.10;
    else return 0.08;
  }
}
