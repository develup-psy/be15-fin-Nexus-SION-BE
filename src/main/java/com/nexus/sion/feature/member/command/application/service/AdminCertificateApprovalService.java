package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

import java.util.List;

public interface AdminCertificateApprovalService {
    List<UserCertificateHistoryResponse> getAllCertificates();
    void approveUserCertificate(Long id);
    void rejectUserCertificate(Long id, CertificateRejectRequest request);
}

