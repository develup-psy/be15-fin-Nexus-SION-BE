package com.nexus.sion.feature.project.query.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkRequestQueryDto {
  private Long workId;
  private String employeeId;
  private String projectCode;
  private String projectTitle;
  private String approvalStatus;
  private String rejectedReason;
  private LocalDateTime approvedAt;
  private LocalDateTime createdAt;
  private List<WorkRequestHistoryDto> histories;

  @Getter
  @AllArgsConstructor
  public static class WorkRequestHistoryDto {
    private Long historyId;
    private String functionDescription;
    private List<String> techStackNames;
    private String functionType;
    private String complexity;
  }
}
