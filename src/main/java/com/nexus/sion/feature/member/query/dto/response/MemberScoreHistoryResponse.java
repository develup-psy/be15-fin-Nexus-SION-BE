package com.nexus.sion.feature.member.query.dto.response;

import java.time.LocalDateTime;

public record MemberScoreHistoryResponse(
    String employeeIdentificationNumber,
    int currentTechScore,
    int currentCertificateScore,
    int currentTotalScore,
    LocalDateTime currentTotalScoreDate,
    Integer previousTechScore,
    LocalDateTime previousTechScoreDate,
    Integer previousCertificateScore,
    LocalDateTime previousCertificateScoreDate,
    Integer previousTotalScore,
    LocalDateTime previousTotalScoreDate) {}
