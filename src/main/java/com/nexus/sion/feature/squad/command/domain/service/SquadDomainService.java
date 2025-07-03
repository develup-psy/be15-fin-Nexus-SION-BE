package com.nexus.sion.feature.squad.command.domain.service;

import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
import org.springframework.stereotype.Service;

@Service
public class SquadDomainService {

  public String buildRecommendationReason(RecommendationCriteria criteria, EvaluatedSquad squad) {
    int tech = squad.getAverageTechStackScore();
    double domain = squad.getAverageDomainRelevance();
    int monthlyCost = squad.getTotalMonthlyCost();
    int estimatedDuration = squad.getEstimatedDuration();
    int totalCost = squad.getEstimatedTotalCost();
    String preCondition = squad.getReason(); // 예: "예산과 기간 조건을 만족하는 최적 조합입니다."

    StringBuilder sb = new StringBuilder();

    // 1. 제약 조건 설명
    sb.append("현재 프로젝트에는 ");
    if (preCondition.contains("예산") && preCondition.contains("기간")) {
      sb.append("예산과 기간의 상한선이 설정되어 있으며, ");
    } else if (preCondition.contains("예산")) {
      sb.append("예산 상한선이 설정되어 있으며, ");
    } else if (preCondition.contains("기간")) {
      sb.append("기간 상한선이 설정되어 있으며, ");
    } else {
      sb.append("예산 및 기간에 대한 제약 없이, ");
    }

    // 2. 추천 기준 설명
    sb.append("당신이 선택한 기준인 ‘").append(getKoreanLabel(criteria)).append("’에 따라, ");

    // 3. 선택된 조합의 이유 중심 설명
    sb.append(getRationale(criteria));

    // 4. 수치 기반 보조 설명 (정량 근거)
    sb.append(String.format(" (기술 점수 평균: %d점, 도메인 적합률: %.0f%%, 예상 기간: %d개월, 총예산: %,d만원)",
            tech, domain * 100, estimatedDuration, totalCost));

    return sb.toString();
  }

  private String getKoreanLabel(RecommendationCriteria criteria) {
    return switch (criteria) {
      case TECH_STACK -> "기술 스택 중심";
      case DOMAIN_MATCH -> "도메인 적합도 중심";
      case COST_OPTIMIZED -> "비용 최적화";
      case TIME_OPTIMIZED -> "기간 최적화";
      case BALANCED -> "균형 추천";
    };
  }

  private String getRationale(RecommendationCriteria criteria) {
    return switch (criteria) {
      case TECH_STACK -> "기술 스택 점수를 최적화하여 가장 높은 기술 역량을 가진 조합을 선정했습니다.";
      case DOMAIN_MATCH -> "해당 프로젝트 도메인에 가장 유사한 경험을 가진 구성원들로 조합을 구성했습니다.";
      case COST_OPTIMIZED -> "최소 비용으로 프로젝트를 수행할 수 있도록 가장 비용 효율적인 인력을 구성했습니다.";
      case TIME_OPTIMIZED -> "가장 빠른 기간 내에 프로젝트를 완료할 수 있는 인력을 구성했습니다.";
      case BALANCED -> "기술, 도메인, 비용, 기간을 모두 균등하게 평가하여 균형 잡힌 최적 조합을 구성했습니다.";
    };
  }
}
