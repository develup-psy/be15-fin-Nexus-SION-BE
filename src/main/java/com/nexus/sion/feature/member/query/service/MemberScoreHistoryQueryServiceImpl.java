package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.MemberScoreHistoryResponse;
import com.nexus.sion.feature.member.query.repository.MemberScoreQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberScoreHistoryQueryServiceImpl implements MemberScoreHistoryQueryService {

    private final MemberScoreQueryRepository repository;

    @Override
    public MemberScoreHistoryResponse getScoreHistory(String employeeId) {
        var curr = repository.getLatestRecord(employeeId);
        if (curr == null) return null;

        int currentTech = curr.getTotalTechStackScores();
        int currentCert = curr.getTotalCertificateScores();

        var prevTech = repository.getPreviousTechScoreChangedRecord(employeeId, currentTech);
        var prevCert = repository.getPreviousCertificateScoreChangedRecord(employeeId, currentCert);

        Integer previousTechScore = prevTech != null ? prevTech.getTotalTechStackScores() : null;
        LocalDateTime previousTechScoreDate = prevTech != null ? prevTech.getCreatedAt() : null;

        Integer previousCertScore = prevCert != null ? prevCert.getTotalCertificateScores() : null;
        LocalDateTime previousCertScoreDate = prevCert != null ? prevCert.getCreatedAt() : null;

        Integer previousTotalScore = null;
        LocalDateTime previousTotalScoreDate = null;

        if (prevTech != null || prevCert != null) {
            int tech = previousTechScore != null ? previousTechScore : currentTech;
            int cert = previousCertScore != null ? previousCertScore : currentCert;
            previousTotalScore = tech + cert;

            // 둘 중 더 최신 날짜 사용
            if (prevTech != null && prevCert != null) {
                previousTotalScoreDate = prevTech.getCreatedAt().isAfter(prevCert.getCreatedAt())
                        ? prevTech.getCreatedAt()
                        : prevCert.getCreatedAt();
            } else if (prevTech != null) {
                previousTotalScoreDate = prevTech.getCreatedAt();
            } else if (prevCert != null) {
                previousTotalScoreDate = prevCert.getCreatedAt();
            }
        }

        return new MemberScoreHistoryResponse(
                curr.getEmployeeIdentificationNumber(),
                currentTech,
                currentCert,
                currentTech + currentCert,
                curr.getCreatedAt(),

                previousTechScore,
                previousTechScoreDate,

                previousCertScore,
                previousCertScoreDate,

                previousTotalScore,
                previousTotalScoreDate
        );
    }
}