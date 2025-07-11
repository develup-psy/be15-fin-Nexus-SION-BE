package com.nexus.sion.feature.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CertificateRejectRequest {
  private String rejectedReason;
}
