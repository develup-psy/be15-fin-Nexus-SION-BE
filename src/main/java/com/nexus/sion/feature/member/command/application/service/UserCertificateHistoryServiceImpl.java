package com.nexus.sion.feature.member.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.UserCertificateHistoryRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.UserCertificateHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.CertificateStatus;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import com.nexus.sion.feature.member.command.domain.repository.UserCertificateHistoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserCertificateHistoryServiceImpl implements UserCertificateHistoryService {

  private final UserCertificateHistoryRepository userCertificateHistoryRepository;
  private final CertificateRepository certificateRepository;

  @Override
  public void registerUserCertificate(String employeeId, UserCertificateHistoryRequest request) {
    // 자격증명으로 certificate 조회
    Certificate certificate =
        certificateRepository
            .findById(request.getCertificateName())
            .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND));

    UserCertificateHistory history =
        UserCertificateHistory.builder()
            .certificateName(certificate.getCertificateName())
            .employeeIdentificationNumber(employeeId)
            .certificateStatus(CertificateStatus.PENDING)
            .pdfFileUrl(request.getPdfFileUrl())
            .issueDate(request.getIssueDate())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    userCertificateHistoryRepository.save(history);
  }
}
