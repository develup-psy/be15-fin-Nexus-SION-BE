package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.command.application.dto.request.UserCertificateHistoryRequest;
import com.nexus.sion.feature.member.query.dto.response.CertificateResponse;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

import java.util.List;

public interface UserCertificateHistoryService {
  void registerUserCertificate(String employeeId, UserCertificateHistoryRequest request);

  List<UserCertificateHistoryResponse> getAllCertificates();

  void approveUserCertificate(Long userCertificateHistoryId);

  void rejectUserCertificate(Long userCertificateHistoryId, CertificateRejectRequest request);
}
