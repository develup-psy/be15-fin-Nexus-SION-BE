package com.nexus.sion.feature.member.query.service;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

public interface UserCertificateHistoryQueryService {
  List<UserCertificateHistoryResponse> getMyCertificates(Long memberId);
}
