package com.nexus.sion.feature.member.command.application.dto.request;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;

import java.math.BigDecimal;

public class GradeDto {
    private GradeCode gradeCode;
    private BigDecimal productivity;
    private int monthlyUnitPrice;
}
