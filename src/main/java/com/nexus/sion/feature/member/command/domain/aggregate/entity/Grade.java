package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Grade {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "grade_code", nullable = false, length = 1)
    private GradeCode gradeCode;

    @Column(name = "min_score", nullable = false)
    private int minScore;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "productivity", nullable = false, precision = 10, scale = 4)
    private BigDecimal productivity;

    @Column(name = "monthly_unit_price", nullable = false)
    private int monthlyUnitPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
