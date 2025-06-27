package com.nexus.sion.feature.statistics.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JobParticipationStatsDto {
  private String jobName;
  private int memberCount;
  private String topTechStack1;
  private String topTechStack2;
}
