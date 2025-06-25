package com.nexus.sion.feature.member.query.dto.response;

import java.time.LocalDate;

public record MemberDetailResponse(
    String employeeId,
    String name,
    String profileImageUrl,
    String phoneNumber,
    String position,
    String department,
    LocalDate birthday,
    LocalDate joinedAt,
    String email,
    Integer careerYears,
    Long salary,
    String status,
    String grade,
    String role) {}
