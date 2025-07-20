package com.nexus.sion.feature.member.query.dto.response;

import java.math.BigDecimal;

public record MemberSquadListResponse(
    String employeeId,
    String name,
    String grade,
    String status,
    String topTechStackName,
    int monthlyUnitPrice,
    BigDecimal productivity,
    String imageUrl) {}
