package com.nexus.sion.feature.member.command.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.MemberScoreHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.UserCertificateHistory;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import com.nexus.sion.feature.member.command.domain.repository.MemberScoreHistoryRepository;
import com.nexus.sion.feature.member.command.domain.repository.UserCertificateHistoryRepository;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCertificateApprovalServiceImpl implements AdminCertificateApprovalService {

  private final UserCertificateHistoryRepository userCertificateHistoryRepository;
  private final CertificateRepository certificateRepository;
  private final MemberScoreHistoryRepository memberScoreHistoryRepository;
  private final NotificationCommandService notificationCommandService;

  @Override
  @Transactional
  public List<UserCertificateHistoryResponse> getAllCertificates() {
    return userCertificateHistoryRepository.findAll().stream()
        .map(UserCertificateHistoryResponse::fromEntity)
        .toList();
  }

  @Override
  @Transactional
  public void approveUserCertificate(Long id) {
    UserCertificateHistory history =
        userCertificateHistoryRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_CERTIFICATE_NOT_FOUND));

    history.approve();
    userCertificateHistoryRepository.save(history);

    // 자격증 점수 가져오기
    Certificate certificate =
        certificateRepository
            .findById(history.getCertificateName())
            .orElseThrow(() -> new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND));
    int certificateScore = certificate.getScore();

    String employeeId = history.getEmployeeIdentificationNumber();

    // 기존 최신 점수 이력 조회
    MemberScoreHistory latest =
        memberScoreHistoryRepository
            .findTopByEmployeeIdentificationNumberOrderByCreatedAtDesc(employeeId)
            .orElseGet(() -> MemberScoreHistory.initial(employeeId));

    // 새로운 점수 이력 저장
    MemberScoreHistory newHistory =
        MemberScoreHistory.builder()
            .employeeIdentificationNumber(employeeId)
            .totalTechStackScores(latest.getTotalTechStackScores())
            .totalCertificateScores(latest.getTotalCertificateScores() + certificateScore)
            .build();

    memberScoreHistoryRepository.save(newHistory);

    // 알림
    notificationCommandService.createAndSendNotification(
        null, employeeId, null, NotificationType.CERTIFICATION_APPROVED, null);
  }

  @Override
  @Transactional
  public void rejectUserCertificate(Long id, CertificateRejectRequest request) {
    UserCertificateHistory history =
        userCertificateHistoryRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_CERTIFICATE_NOT_FOUND));

    history.reject(request.getRejectedReason());
    userCertificateHistoryRepository.save(history);

    // 알림
    notificationCommandService.createAndSendNotification(
        null,
        history.getEmployeeIdentificationNumber(),
        null,
        NotificationType.CERTIFICATION_REJECTED,
        null);
  }
}
