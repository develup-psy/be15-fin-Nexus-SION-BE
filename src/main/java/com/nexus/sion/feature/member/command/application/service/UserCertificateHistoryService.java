package com.nexus.sion.feature.member.command.application.service;

import java.util.List;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.command.application.dto.request.UserCertificateHistoryRequest;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

public interface UserCertificateHistoryService {
  void registerUserCertificate(String employeeId, UserCertificateHistoryRequest request);

  List<UserCertificateHistoryResponse> getAllCertificates();

  void approveUserCertificate(Long userCertificateHistoryId);

  void rejectUserCertificate(Long userCertificateHistoryId, CertificateRejectRequest request);
}
