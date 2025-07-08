package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.CertificateResponse;

import java.util.List;

public interface CertificateQueryService {
    List<CertificateResponse> getAllCertificates();
}
