package com.nexus.sion.feature.statistics.query.dto;

import com.example.jooq.generated.enums.MemberGradeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GradeSalaryStatsDto {
    private MemberGradeCode gradeCode;
    private Long minSalary;
    private Long maxSalary;
    private Long avgSalary;
}
