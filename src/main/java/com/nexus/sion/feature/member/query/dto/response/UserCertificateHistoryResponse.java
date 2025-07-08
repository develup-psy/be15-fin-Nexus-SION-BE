package com.nexus.sion.feature.member.query.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCertificateHistoryResponse {

  private Long userCertificateHistoryId;
  private String certificateName;
  private String issuingOrganization;
  private LocalDateTime issueDate;
  private String pdfFileUrl;
  private String certificateStatus;
  private String rejectedReason;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
