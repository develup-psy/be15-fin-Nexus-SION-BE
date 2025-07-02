package com.nexus.sion.feature.squad.command.domain.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;

@Service
public class SquadSelectorImpl {

  public EvaluatedSquad selectBest(List<EvaluatedSquad> squads, RecommendationCriteria criteria) {
    double minCost = squads.stream().mapToDouble(EvaluatedSquad::getTotalMonthlyCost).min().orElse(0);
    double maxCost = squads.stream().mapToDouble(EvaluatedSquad::getTotalMonthlyCost).max().orElse(1);
    double minDuration = squads.stream().mapToDouble(EvaluatedSquad::getEstimatedDuration).min().orElse(0);
    double maxDuration = squads.stream().mapToDouble(EvaluatedSquad::getEstimatedDuration).max().orElse(1);

    return squads.stream()
            .max(Comparator.comparingDouble(
                    squad -> calculateScore(squad, criteria, minCost, maxCost, minDuration, maxDuration)))
            .orElseThrow(() -> new IllegalArgumentException("추천 가능한 스쿼드가 없습니다."));
  }


  private double calculateScore(
          EvaluatedSquad squad,
          RecommendationCriteria criteria,
          double minCost,
          double maxCost,
          double minDuration,
          double maxDuration){

    // 기준 점수 항목 정규화 (높을수록 좋은 항목은 정규화, 낮을수록 좋은 항목은 역정규화)
    double tech = squad.getAverageTechStackScore(); // 높을수록 좋음
    double domain = squad.getAverageDomainRelevance(); // 높을수록 좋음
    double cost = squad.getTotalMonthlyCost(); // 낮을수록 좋음
    double duration = squad.getEstimatedDuration(); // 낮을수록 좋음

    // 가중치 설정
    double techWeight = 0.25;
    double domainWeight = 0.25;
    double costWeight = 0.25;
    double durationWeight = 0.25;

    switch (criteria) {
      case TECH_STACK -> techWeight = 0.4;
      case DOMAIN_MATCH -> domainWeight = 0.4;
      case COST_OPTIMIZED -> costWeight = 0.4;
      case TIME_OPTIMIZED -> durationWeight = 0.4;
      case BALANCED -> {} // default 균형
    }

    double normalizedCost;
    if (maxCost != minCost) {
      normalizedCost = (cost - minCost) / (maxCost - minCost);
    } else {
      normalizedCost = 0.0; // 모든 값이 같을 경우 점수 차등이 없음
    }
    double costScore = 1 - normalizedCost;

    double normalizedDuration;
    if (maxDuration != minDuration) {
      normalizedDuration = (duration - minDuration) / (maxDuration - minDuration);
    } else {
      normalizedDuration = 0.0;
    }
    double durationScore = 1 - normalizedDuration;

    return tech * techWeight
        + domain * domainWeight
        + costScore * costWeight
        + durationScore * durationWeight;
  }
}
