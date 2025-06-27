package com.nexus.sion.feature.member.command.application.dto.request;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import lombok.*;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class UnitPriceSetRequest {
    private GradeCode gradeCode;
    private BigDecimal productivity;
    private int monthlyUnitPrice;
}
