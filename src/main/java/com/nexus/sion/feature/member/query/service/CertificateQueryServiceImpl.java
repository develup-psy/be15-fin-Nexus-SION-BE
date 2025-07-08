package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import com.nexus.sion.feature.member.query.dto.response.CertificateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateQueryServiceImpl implements CertificateQueryService {

    private final CertificateRepository certificateRepository;

    @Override
    public List<CertificateResponse> getAllCertificates() {
        return certificateRepository.findAll().stream()
            .map(cert -> new CertificateResponse(
                cert.getCertificateName(),
                cert.getScore(),
                cert.getIssuingOrganization()
            ))
            .toList();
    }
}
