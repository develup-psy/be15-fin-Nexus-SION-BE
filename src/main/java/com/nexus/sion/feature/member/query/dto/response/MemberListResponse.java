package com.nexus.sion.feature.member.query.dto.response;

import java.time.LocalDate;

public record MemberListResponse(
    String employeeId,
    String name,
    String phoneNumber,
    String email,
    String role,
    String grade_code,
    String status,
    String profileImageUrl,
    LocalDate joinedAt,
    String topTechStackName,
    Integer careerYears) {}
