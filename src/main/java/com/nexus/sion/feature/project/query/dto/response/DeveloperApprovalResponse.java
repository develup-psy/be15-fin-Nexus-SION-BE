package com.nexus.sion.feature.project.query.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperApprovalResponse {
    private String developerProjectWorkId;
    private String employeeIdentificationNumber;
    private String approvalStatus;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectedReason;
}

