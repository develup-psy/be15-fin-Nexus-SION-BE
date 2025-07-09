package com.nexus.sion.feature.member.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateCreateRequest;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateUpdateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateCommandServiceImpl implements CertificateCommandService {

  private final CertificateRepository certificateRepository;

  @Override
  public void registerCertificate(CertificateCreateRequest request) {
    Certificate certificate =
        Certificate.builder()
            .certificateName(request.getCertificateName())
            .issuingOrganization(request.getIssuingOrganization())
            .score(request.getScore())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    certificateRepository.save(certificate);
  }

  @Transactional
  @Override
  public void updateCertificate(String certificateName, CertificateUpdateRequest request) {
    Certificate certificate = findCertificateByName(certificateName);
    certificate.update(request.getScore(), request.getIssuingOrganization());
  }

  @Transactional
  @Override
  public void deleteCertificate(String certificateName) {
    Certificate certificate = findCertificateByName(certificateName);
    certificateRepository.delete(certificate);
  }

  // 중복 제거를 위한 private 메서드 추출
  private Certificate findCertificateByName(String certificateName) {
    return certificateRepository
            .findById(certificateName)
            .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND));
  }
}
