package com.nexus.sion.feature.member.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
}
