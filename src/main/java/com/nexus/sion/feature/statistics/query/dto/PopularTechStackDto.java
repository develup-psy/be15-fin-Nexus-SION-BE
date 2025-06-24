package com.nexus.sion.feature.statistics.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PopularTechStackDto {
  private String techStackName;
  private int usageCount;
  private String latestProjectName;
  private String topJobName;
}
