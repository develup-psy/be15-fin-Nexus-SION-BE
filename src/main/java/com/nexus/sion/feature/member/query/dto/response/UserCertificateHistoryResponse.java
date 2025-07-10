package com.nexus.sion.feature.member.query.dto.response;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.UserCertificateHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.CertificateStatus;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCertificateHistoryResponse {

  private Long userCertificateHistoryId;
  private String certificateName;
  private String issuingOrganization;
  private String employeeIdentificationNumber;
  private String employeeName;
  private LocalDate issueDate;
  private String pdfFileUrl;
  private String certificateStatus;
  private String rejectedReason;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static UserCertificateHistoryResponse fromEntity(UserCertificateHistory history) {
    return UserCertificateHistoryResponse.builder()
            .userCertificateHistoryId(history.getId())
            .certificateName(history.getCertificateName())
            .issuingOrganization(history.getCertificate() != null ? history.getCertificate().getIssuingOrganization() : null)
            .employeeIdentificationNumber(history.getEmployeeIdentificationNumber())
            .employeeName(history.getMember() != null ? history.getMember().getEmployeeName() : null)
            .issueDate(history.getIssueDate())
            .pdfFileUrl(history.getPdfFileUrl())
            .certificateStatus(history.getCertificateStatus().name())
            .rejectedReason(history.getRejectedReason())
            .createdAt(history.getCreatedAt())
            .updatedAt(history.getUpdatedAt())
            .build();
  }
}
