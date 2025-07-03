package com.nexus.sion.feature.squad.command.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import com.nexus.sion.feature.project.command.domain.repository.ProjectFpSummaryRepository;
import com.nexus.sion.feature.squad.command.application.dto.internal.CandidateSummary;
import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SquadEvaluatorImpl {

  private static final double DEFAULT_MAN_MONTH_PER_FP = 0.3;

  private final ProjectFpSummaryRepository projectFpSummaryRepository;

  public List<EvaluatedSquad> evaluateAll(
      String projectId, List<Map<String, List<CandidateSummary>>> squadCombinations) {

    int totalFP =
        projectFpSummaryRepository
            .findByProjectCode(projectId)
            .map(ProjectFpSummary::getTotalFp)
            .orElseThrow(() -> new BusinessException(ErrorCode.FP_NOT_FOUND));

    int totalManMonth = (int) Math.ceil(totalFP * DEFAULT_MAN_MONTH_PER_FP);
    List<EvaluatedSquad> results = new ArrayList<>();

    for (Map<String, List<CandidateSummary>> squad : squadCombinations) {
      List<CandidateSummary> allMembers = squad.values().stream().flatMap(List::stream).toList();

      int totalTechScore = allMembers.stream().mapToInt(CandidateSummary::getTechStackScore).sum();
      double totalDomainScore =
          allMembers.stream().mapToDouble(CandidateSummary::getDomainRelevance).sum();
      int totalMonthlyCost = allMembers.stream().mapToInt(CandidateSummary::getCostPerMonth).sum();
      double totalProductivity =
          allMembers.stream().mapToDouble(CandidateSummary::getProductivityFactor).sum();

      int avgTech = totalTechScore / allMembers.size();
      double avgDomain = totalDomainScore / allMembers.size();

      int estimatedDuration = (int) Math.ceil(totalManMonth / totalProductivity);

      results.add(
          new EvaluatedSquad(
              squad, avgTech, avgDomain, totalMonthlyCost, totalManMonth, estimatedDuration));
    }

    return results;
  }
}
