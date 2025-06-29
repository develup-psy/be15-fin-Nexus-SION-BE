package com.nexus.sion.feature.member.command.application.dto.request;

import java.math.BigDecimal;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class GradeDto {
  private GradeCode gradeCode;
  private BigDecimal productivity;
  private Integer monthlyUnitPrice;
}
