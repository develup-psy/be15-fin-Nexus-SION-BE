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
    // 점수 계산
    Map<EvaluatedSquad, Double> techScores = assignQuantileScore(squads, s -> (double) s.getAverageTechStackScore(), true);
    Map<EvaluatedSquad, Double> domainScores = assignQuantileScore(squads, EvaluatedSquad::getAverageDomainRelevance, true);
    Map<EvaluatedSquad, Double> costScores = assignNormalizedScore(squads, s -> (double) s.getTotalMonthlyCost(), false); // 정규화
    Map<EvaluatedSquad, Double> durationScores = assignQuantileScore(squads, s -> (double) s.getEstimatedDuration(), false);

    // 히스토그램 출력
    printHistogram("기술 점수 분포", squads, s -> (double) s.getAverageTechStackScore(), 5);
    printHistogram("도메인 적합도 분포", squads, EvaluatedSquad::getAverageDomainRelevance, 5);
    printHistogram("예산 분포", squads, s -> (double) s.getTotalMonthlyCost(), 5);
    printHistogram("예상 기간 분포", squads, s -> (double) s.getEstimatedDuration(), 5);

    // 최적 스쿼드 선택
    return squads.stream()
            .max(Comparator.comparingDouble(squad ->
                    calculateScore(squad, criteria, techScores, domainScores, costScores, durationScores)))
            .orElseThrow(() -> new IllegalArgumentException("추천 가능한 스쿼드가 없습니다."));
  }

  private double calculateScore(
          EvaluatedSquad squad,
          RecommendationCriteria criteria,
          Map<EvaluatedSquad, Double> techScores,
          Map<EvaluatedSquad, Double> domainScores,
          Map<EvaluatedSquad, Double> costScores,
          Map<EvaluatedSquad, Double> durationScores
  ) {
    double tech = techScores.getOrDefault(squad, 0.0);
    double domain = domainScores.getOrDefault(squad, 0.0);
    double cost = costScores.getOrDefault(squad, 0.0);
    double duration = durationScores.getOrDefault(squad, 0.0);

    // 기본 가중치
    double techWeight = 0.1;
    double domainWeight = 0.1;
    double costWeight = 0.1;
    double durationWeight = 0.1;

    // 선택 기준에 따라 특정 항목 가중치 조정
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

    return tech * techWeight + domain * domainWeight + cost * costWeight + duration * durationWeight;
  }

  private Map<EvaluatedSquad, Double> assignQuantileScore(
          List<EvaluatedSquad> squads,
          Function<EvaluatedSquad, Double> extractor,
          boolean ascending
  ) {
    int size = squads.size();
    List<EvaluatedSquad> sorted = squads.stream()
            .sorted(Comparator.comparingDouble(extractor::apply))
            .collect(Collectors.toList());

    Map<EvaluatedSquad, Double> result = new HashMap<>();
    for (int i = 0; i < size; i++) {
      double quantileScore = ((double) i) / size;
      double rounded = Math.ceil(quantileScore * 5) / 5.0; // 분위수 (0.2 단위)
      double score = ascending ? rounded : 1.0 - rounded + 0.2;
      result.put(sorted.get(i), score);
    }
    return result;
  }

  // 예산에만 사용하는 최소-최대 정규화
  private Map<EvaluatedSquad, Double> assignNormalizedScore(
          List<EvaluatedSquad> squads,
          Function<EvaluatedSquad, Double> extractor,
          boolean ascending
  ) {
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

  private void printHistogram(String title, List<EvaluatedSquad> squads, Function<EvaluatedSquad, Double> extractor, int bins) {
    System.out.println("=== [" + title + "] ===");
    double min = squads.stream().mapToDouble(extractor::apply).min().orElse(0);
    double max = squads.stream().mapToDouble(extractor::apply).max().orElse(1);
    double interval = (max - min) / bins;

    Map<Integer, Long> histogram = squads.stream()
            .map(extractor)
            .map(score -> {
              int bin = (int) ((score - min) / interval);
              return Math.min(bin, bins - 1);
            })
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    for (int i = 0; i < bins; i++) {
      double start = min + i * interval;
      double end = (i == bins - 1) ? max : start + interval;
      long count = histogram.getOrDefault(i, 0L);
      System.out.printf("%.2f ~ %.2f : %d개%n", start, end, count);
    }
  }
}
