package com.nexus.sion.feature.member.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.common.s3.service.DocumentS3Service;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.UserCertificateHistoryRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.UserCertificateHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.CertificateStatus;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import com.nexus.sion.feature.member.command.domain.repository.UserCertificateHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeveloperCertificateHistoryServiceImpl implements DeveloperCertificateHistoryService {

  private final CertificateRepository certificateRepository;
  private final UserCertificateHistoryRepository userCertificateHistoryRepository;
  private final DocumentS3Service documentS3Service;

  @Override
  @Transactional
  public void registerUserCertificate(String employeeId, UserCertificateHistoryRequest request) {
    Certificate certificate =
        certificateRepository
            .findById(request.getCertificateName())
            .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND));

    String uploadedUrl =
        documentS3Service.uploadFile(request.getPdfFileUrl(), "certificates").getUrl();

    UserCertificateHistory history =
        UserCertificateHistory.builder()
            .certificateName(certificate.getCertificateName())
            .employeeIdentificationNumber(employeeId)
            .certificateStatus(CertificateStatus.PENDING)
            .pdfFileUrl(uploadedUrl)
            .issueDate(request.getIssueDate())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    userCertificateHistoryRepository.save(history);
  }
}
