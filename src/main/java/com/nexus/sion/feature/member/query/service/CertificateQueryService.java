package com.nexus.sion.feature.member.query.service;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.CertificateResponse;

public interface CertificateQueryService {
  List<CertificateResponse> getAllCertificates();
}
