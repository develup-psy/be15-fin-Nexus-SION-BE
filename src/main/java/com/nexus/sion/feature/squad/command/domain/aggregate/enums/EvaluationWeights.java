package com.nexus.sion.feature.squad.command.domain.aggregate.enums;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EvaluationWeights {
  BASIC(0.25, 0.25, 0.25, 0.25), // A안: 균형
  BUDGET(0.20, 0.40, 0.20, 0.20), // C안: 예산 중심
  DURATION(0.40, 0.20, 0.20, 0.20), // B안: 기간 중심
  TECH_STACK(0.20, 0.20, 0.40, 0.20), // E안: 기술스택 중심
  DOMAIN(0.20, 0.20, 0.20, 0.40); // D안: 도메인 중심

  private final double durationWeight;
  private final double costWeight;
  private final double techWeight;
  private final double domainWeight;

  public Map<String, Double> toMap() {
    return Map.of(
        "duration", durationWeight,
        "cost", costWeight,
        "tech", techWeight,
        "domain", domainWeight);
  }
}
