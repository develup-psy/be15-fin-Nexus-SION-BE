package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;
import com.nexus.sion.feature.member.query.repository.UserCertificateHistoryQueryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCertificateHistoryQueryServiceImpl implements UserCertificateHistoryQueryService {

    private final UserCertificateHistoryQueryRepository userCertificateHistoryQueryRepository;

    @Override
    public List<UserCertificateHistoryResponse> getMyCertificates(Long memberId) {
        return userCertificateHistoryQueryRepository.findByMemberId(memberId);
    }
}
