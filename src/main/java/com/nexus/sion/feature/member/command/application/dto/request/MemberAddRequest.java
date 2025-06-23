package com.nexus.sion.feature.member.command.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MemberAddRequest(
        @NotBlank String employeeIdentificationNumber,
        @NotBlank String employeeName,
        @NotBlank String phoneNumber,
        @NotNull LocalDate birthday,
        @NotNull LocalDateTime joinedAt,
        @NotBlank @Email String email,
        @NotNull Integer careerYears,

        // 선택 입력
        String positionName,
        String departmentName,
        String profileImageUrl,
        Long salary,
        List<String> techStackNames
) {}