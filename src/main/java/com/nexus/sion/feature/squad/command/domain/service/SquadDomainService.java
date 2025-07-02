package com.nexus.sion.feature.squad.command.domain.service;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;

@Service
public class SquadDomainService {
  public String buildRecommendationReason(RecommendationCriteria criteria, EvaluatedSquad squad) {
    return switch (criteria) {
      case BALANCED ->
          "기술 점수, 도메인 적합도, 단가, 기간을 균형 있게 고려한 결과로 추천되었습니다. "
              + "기술점수 평균: "
              + squad.getAverageTechStackScore()
              + ", 도메인 적합도 평균: "
              + String.format("%.2f", squad.getAverageDomainRelevance());

      case TECH_STACK ->
          "기술 스택 점수를 가장 높은 조합으로 추천하였습니다. " + "기술점수 평균: " + squad.getAverageTechStackScore();

      case DOMAIN_MATCH ->
          "도메인 경험이 가장 유사한 조합으로 추천하였습니다. "
              + "도메인 적합도 평균: "
              + String.format("%.2f", squad.getAverageDomainRelevance());

      case COST_OPTIMIZED ->
          "단가가 가장 낮은 조합으로 추천하였습니다. " + "예상 총비용: " + squad.getTotalMonthlyCost() + "만원";

      case TIME_OPTIMIZED ->
          "예상 기간이 가장 짧은 조합으로 추천하였습니다. " + "예상 기간: " + squad.getEstimatedDuration() + "개월";

      default -> "추천 기준을 기반으로 최적의 조합을 도출하였습니다.";
    };
  }
}
