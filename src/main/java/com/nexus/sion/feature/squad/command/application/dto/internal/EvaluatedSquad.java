package com.nexus.sion.feature.squad.command.application.dto.internal;

import java.util.List;
import java.util.Map;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EvaluatedSquad {
  private Map<String, List<CandidateSummary>> squad;
  private int averageTechStackScore;
  private double averageDomainRelevance;
  private int totalMonthlyCost; // 단가 총합
  private int estimatedManMonth; // 총 인월
  private int estimatedDuration; // 월 단위 기간
  private int estimatedTotalCost;
  private String reason;
}
