package com.nexus.sion.feature.project.query.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperApprovalResponse {
  private Long developerProjectWorkId;
  private String employeeIdentificationNumber;
  private String approvalStatus;
  private String approvedBy;
  private LocalDateTime approvedAt;
  private String rejectedReason;
}
