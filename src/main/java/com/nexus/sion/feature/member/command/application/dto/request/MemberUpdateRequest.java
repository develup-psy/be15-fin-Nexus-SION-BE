package com.nexus.sion.feature.member.command.application.dto.request;

import java.time.LocalDate;
import java.util.List;

public record MemberUpdateRequest(
    String employeeName,
    String phoneNumber,
    LocalDate birthday,
    LocalDate joinedAt,
    String email,
    Integer careerYears,
    String positionName,
    String departmentName,
    String profileImageUrl,
    Long salary,
    List<String> techStackNames) {}
