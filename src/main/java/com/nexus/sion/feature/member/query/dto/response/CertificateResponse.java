package com.nexus.sion.feature.member.query.dto.response;

public record CertificateResponse(
        String certificateName,
        int score,
        String issuingOrganization
) {}
