package com.nexus.sion.feature.member.command.application.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberAddRequest(
    @NotBlank String employeeIdentificationNumber,
    @NotBlank String employeeName,
    @NotBlank String phoneNumber,
    @NotNull LocalDate birthday,
    @NotNull LocalDate joinedAt,
    @NotBlank @Email String email,
    @NotNull Integer careerYears,

    // 선택 입력
    String positionName,
    String departmentName,
    String profileImageUrl,
    Long salary,
    List<String> techStackNames) {}
