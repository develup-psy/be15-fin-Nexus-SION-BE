package com.nexus.sion.feature.member.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.jooq.generated.enums.UserCertificateHistoryCertificateStatus;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;
import com.nexus.sion.feature.member.query.repository.UserCertificateHistoryQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCertificateHistoryQueryServiceImpl implements UserCertificateHistoryQueryService {

  private final UserCertificateHistoryQueryRepository userCertificateHistoryQueryRepository;

  @Override
  public List<UserCertificateHistoryResponse> getMyCertificates(String memberId) {
    return userCertificateHistoryQueryRepository.findByMemberId(memberId);
  }

  // 전체 자격증 이름 조회
  @Override
  public List<String> findAllCertificateNames() {
    return userCertificateHistoryQueryRepository.findAllCertificateNames();
  }

  // 특정 사용자가 보유한 자격증 이름 조회
  @Override
  public List<String> findOwnedCertificateNamesByEmployee(String employeeId) {
    return userCertificateHistoryQueryRepository.findOwnedCertificateNamesByStatus(
        employeeId, UserCertificateHistoryCertificateStatus.APPROVED);
  }
}
