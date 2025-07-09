package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateCreateRequest;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateUpdateRequest;

public interface CertificateCommandService {
  void registerCertificate(CertificateCreateRequest request);

  void updateCertificate(String certificateName, CertificateUpdateRequest request);

  void deleteCertificate(String certificateName);
}
