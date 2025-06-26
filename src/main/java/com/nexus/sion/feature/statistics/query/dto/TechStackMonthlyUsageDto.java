package com.nexus.sion.feature.statistics.query.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechStackMonthlyUsageDto {
  private String techStackName;
  private Map<String, Integer> monthlyUsage; // 예: {"2024-01": 5, "2024-02": 12, ...}
  private int totalUsageCount;
  private String latestProjectName;
  private String topJobName;
}
