package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CertificateCommandServiceImpl implements CertificateCommandService {

    private final CertificateRepository certificateRepository;

    @Override
    public void registerCertificate(CertificateRequest request) {
        Certificate certificate = Certificate.builder()
            .certificateName(request.getCertificateName())
            .score(request.getScore())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        certificateRepository.save(certificate);
    }
}
