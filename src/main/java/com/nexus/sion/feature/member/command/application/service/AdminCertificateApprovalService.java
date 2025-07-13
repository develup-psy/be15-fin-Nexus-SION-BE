package com.nexus.sion.feature.member.command.application.service;

import java.util.List;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

public interface AdminCertificateApprovalService {
  List<UserCertificateHistoryResponse> getAllCertificates();

  void approveUserCertificate(Long id);

  void rejectUserCertificate(Long id, CertificateRejectRequest request);
}
