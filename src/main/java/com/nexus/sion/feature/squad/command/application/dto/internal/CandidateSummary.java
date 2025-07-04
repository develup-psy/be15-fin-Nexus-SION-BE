package com.nexus.sion.feature.squad.command.application.dto.internal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CandidateSummary {
  private String memberId;
  private String name;
  private String jobName;
  private int techStackScore;
  private double domainRelevance;
  private int costPerMonth;
  private double productivityFactor;
}
