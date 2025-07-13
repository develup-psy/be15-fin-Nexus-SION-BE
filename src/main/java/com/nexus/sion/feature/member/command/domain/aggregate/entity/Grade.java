package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import static com.nexus.sion.common.constants.GradeRatioConstants.GRADE_RATIO;

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

  @Column(name = "ratio", nullable = false, precision = 5, scale = 4)
  private BigDecimal ratio;

  @Setter
  @Column(name = "productivity", nullable = false, precision = 10, scale = 4)
  private BigDecimal productivity;

  @Setter
  @Column(name = "monthly_unit_price", nullable = false)
  private int monthlyUnitPrice;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "score_threshold", nullable = false)
  private int scoreThreshold;

  @PrePersist
  public void prePersist() {
    if (this.ratio == null) {
      this.ratio = new BigDecimal(GRADE_RATIO);
    }
  }
  @PreUpdate
  public void preUpdate() {
    this.ratio = new BigDecimal(GRADE_RATIO);
  }
}