package com.nexus.sion.feature.project.query.dto.response;

import java.time.LocalDate;
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
public class WorkInfoQueryDto {
  private Long workId;
  private String employeeId;
  private String projectCode;
  private String projectTitle;
  private String approvalStatus;
  private LocalDateTime approvedAt;
  private LocalDateTime createdAt;
  private LocalDate actualEndDate;
  private List<WorkRequestHistoryDto> histories;

  @Getter
  @AllArgsConstructor
  public static class WorkRequestHistoryDto {
    private Long historyId;
    private String functionName;
    private String functionDescription;
    private List<String> techStackNames;
    private String functionType;
    private Integer det;
    private Integer ftr;
    private String complexity;
  }
}
