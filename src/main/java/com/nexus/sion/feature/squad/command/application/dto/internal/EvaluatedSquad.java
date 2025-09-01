package com.nexus.sion.feature.squad.command.application.dto.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EvaluatedSquad {
  private Map<String, List<DeveloperSummary>> squad;
  private double averageTechStackScore;
  private double averageDomainRelevance;
  private int totalMonthlyCost; // 단가 총합
  private int estimatedManMonth; // 총 인월
  private int estimatedDuration; // 월 단위 기간
  private BigDecimal estimatedTotalCost;
  private String reason;
}
