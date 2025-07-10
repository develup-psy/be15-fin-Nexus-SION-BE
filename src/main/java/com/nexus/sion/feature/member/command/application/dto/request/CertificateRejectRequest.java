package com.nexus.sion.feature.member.command.application.dto.request;

import lombok.Getter;

@Getter
public class CertificateRejectRequest {
  private String rejectedReason;

  public CertificateRejectRequest(String reason) {
    this.rejectedReason = reason;
  }
}
