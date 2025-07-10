package com.nexus.sion.feature.member.query.service;

import java.time.LocalDateTime;

import com.example.jooq.generated.tables.records.MemberScoreHistoryRecord;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.query.dto.response.MemberScoreHistoryResponse;
import com.nexus.sion.feature.member.query.repository.MemberScoreQueryRepository;

import lombok.RequiredArgsConstructor;

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
      MemberScoreHistoryRecord latestPrevRecord;
      if (prevTech == null) {
        latestPrevRecord = prevCert;
      } else if (prevCert == null) {
        latestPrevRecord = prevTech;
      } else {
        latestPrevRecord =
                prevTech.getCreatedAt().isAfter(prevCert.getCreatedAt()) ? prevTech : prevCert;
      }
      if (latestPrevRecord != null) {
        previousTotalScore =
                latestPrevRecord.getTotalTechStackScores() + latestPrevRecord.getTotalCertificateScores();
        previousTotalScoreDate = latestPrevRecord.getCreatedAt();
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
        previousTotalScoreDate);
  }
}
