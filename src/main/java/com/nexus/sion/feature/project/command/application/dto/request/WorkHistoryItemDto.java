package com.nexus.sion.feature.project.command.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkHistoryItemDto {
  private String functionDescription;
  private String techStackName;
  private String functionType; // 예: "EI", "EO", ...
  private String complexity; // 예: "SIMPLE", "MEDIUM", ...
}
