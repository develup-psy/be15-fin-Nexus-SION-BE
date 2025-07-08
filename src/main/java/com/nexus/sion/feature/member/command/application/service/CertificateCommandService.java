package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateRequest;

public interface CertificateCommandService {
  void registerCertificate(CertificateRequest request);
}
