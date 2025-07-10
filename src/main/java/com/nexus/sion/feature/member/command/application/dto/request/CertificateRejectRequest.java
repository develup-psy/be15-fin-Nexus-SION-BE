package com.nexus.sion.feature.member.command.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CertificateRejectRequest {
  private String rejectedReason;
}
