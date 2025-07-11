package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

import java.util.List;

public interface UserCertificateHistoryQueryService {
    List<UserCertificateHistoryResponse> getMyCertificates(Long memberId);
}
