package com.nexus.sion.feature.project.command.domain.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "project_fp_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectFpSummary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "project_fp_summary_id")
  private Long id;

  @Column(name = "total_fp", nullable = false)
  private int totalFp;

  @Column(name = "avg_effort_per_fp", nullable = false)
  private int avgEffortPerFp;

  @Column(name = "total_effort")
  private BigDecimal totalEffort;

  @Column(name = "estimated_duration")
  private BigDecimal estimatedDuration;

  @Column(name = "estimated_cost")
  private BigDecimal estimatedCost;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "project_code", nullable = false, length = 30)
  private String projectCode;
}
