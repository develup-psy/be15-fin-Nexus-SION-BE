package com.nexus.sion.feature.member.command.application.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MemberUpdateRequest(
        String employeeName,
        String phoneNumber,
        LocalDate birthday,
        LocalDateTime joinedAt,
        String email,
        Integer careerYears,
        String positionName,
        String departmentName,
        String profileImageUrl,
        Long salary,
        List<String> techStackNames
) {}