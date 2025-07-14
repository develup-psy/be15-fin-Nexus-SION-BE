package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.UserCertificateHistoryRequest;

public interface DeveloperCertificateHistoryService {
  void registerUserCertificate(String employeeId, UserCertificateHistoryRequest request);
}
