// WorkHistoryAddRequestDto.java
package com.nexus.sion.feature.project.command.application.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkHistoryAddRequestDto {
  private Long workId;
  private List<WorkHistoryItemDto> histories;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WorkHistoryItemDto {
    private String functionName;
    private String functionDescription;
    private String functionType;
    private String det;
    private String ftr;
    private List<String> techStackNames;
  }
}
