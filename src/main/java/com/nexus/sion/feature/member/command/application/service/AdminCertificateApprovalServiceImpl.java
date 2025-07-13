package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.UserCertificateHistory;
import com.nexus.sion.feature.member.command.domain.repository.UserCertificateHistoryRepository;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCertificateApprovalServiceImpl implements AdminCertificateApprovalService {

    private final UserCertificateHistoryRepository userCertificateHistoryRepository;

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
        UserCertificateHistory history = userCertificateHistoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CERTIFICATE_NOT_FOUND));
        history.approve();
        userCertificateHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void rejectUserCertificate(Long id, CertificateRejectRequest request) {
        UserCertificateHistory history = userCertificateHistoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CERTIFICATE_NOT_FOUND));
        history.reject(request.getRejectedReason());
        userCertificateHistoryRepository.save(history);
    }
}

