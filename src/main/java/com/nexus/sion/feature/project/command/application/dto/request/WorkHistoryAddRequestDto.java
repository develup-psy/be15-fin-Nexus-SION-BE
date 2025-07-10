// WorkHistoryAddRequestDto.java
package com.nexus.sion.feature.project.command.application.dto.request;

import java.util.List;

import lombok.Getter;

@Getter
public class WorkHistoryAddRequestDto {
  private Long workId;
  private List<WorkHistoryItemDto> histories;

  @Getter
  public static class WorkHistoryItemDto {
    private String functionName;
    private String functionDescription;
    private String functionType;
    private String det;
    private String ftr;
    private List<String> techStackNames;
  }
}
