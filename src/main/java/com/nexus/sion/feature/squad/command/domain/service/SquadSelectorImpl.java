package com.nexus.sion.feature.squad.command.domain.service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;

@Service
public class SquadSelectorImpl {

  public EvaluatedSquad selectBest(List<EvaluatedSquad> squads, RecommendationCriteria criteria) {
    Map<EvaluatedSquad, Double> techScores =
            assignNormalizedScore(squads, s -> (double) s.getAverageTechStackScore(), true);
    Map<EvaluatedSquad, Double> domainScores =
            assignNormalizedScore(squads, EvaluatedSquad::getAverageDomainRelevance, true);
    Map<EvaluatedSquad, Double> costScores =
            assignNormalizedScore(squads, s -> (double) s.getTotalMonthlyCost(), false);
    Map<EvaluatedSquad, Double> durationScores =
            assignNormalizedScore(squads, s -> (double) s.getEstimatedDuration(), false);

    return squads.stream()
            .max(
                    Comparator.comparingDouble(
                            squad ->
                                    calculateScore(
                                            squad, criteria, techScores, domainScores, costScores, durationScores)))
            .orElseThrow(() -> new IllegalArgumentException("추천 가능한 스쿼드가 없습니다."));
  }

  private double calculateScore(
          EvaluatedSquad squad,
          RecommendationCriteria criteria,
          Map<EvaluatedSquad, Double> techScores,
          Map<EvaluatedSquad, Double> domainScores,
          Map<EvaluatedSquad, Double> costScores,
          Map<EvaluatedSquad, Double> durationScores) {
    double tech = techScores.getOrDefault(squad, 0.0);
    double domain = domainScores.getOrDefault(squad, 0.0);
    double cost = costScores.getOrDefault(squad, 0.0);
    double duration = durationScores.getOrDefault(squad, 0.0);

    double techWeight = 0.1;
    double domainWeight = 0.1;
    double costWeight = 0.1;
    double durationWeight = 0.1;

    switch (criteria) {
      case TECH_STACK -> techWeight = 0.7;
      case DOMAIN_MATCH -> domainWeight = 0.7;
      case COST_OPTIMIZED -> costWeight = 0.7;
      case TIME_OPTIMIZED -> durationWeight = 0.7;
      case BALANCED -> {
        techWeight = 0.25;
        domainWeight = 0.25;
        costWeight = 0.25;
        durationWeight = 0.25;
      }
    }

    return tech * techWeight
            + domain * domainWeight
            + cost * costWeight
            + duration * durationWeight;
  }

  private Map<EvaluatedSquad, Double> assignNormalizedScore(
          List<EvaluatedSquad> squads, Function<EvaluatedSquad, Double> extractor, boolean ascending) {
    double min = squads.stream().mapToDouble(extractor::apply).min().orElse(0);
    double max = squads.stream().mapToDouble(extractor::apply).max().orElse(1);
    double range = max - min == 0 ? 1 : max - min;

    Map<EvaluatedSquad, Double> result = new HashMap<>();
    for (EvaluatedSquad squad : squads) {
      double raw = extractor.apply(squad);
      double normalized = (raw - min) / range;
      double score = ascending ? normalized : 1.0 - normalized;
      result.put(squad, score);
    }
    return result;
  }
}
