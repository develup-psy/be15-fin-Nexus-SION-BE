package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.MemberScoreHistoryResponse;

public interface MemberScoreHistoryQueryService {
    MemberScoreHistoryResponse getScoreHistory(String employeeId);
}
