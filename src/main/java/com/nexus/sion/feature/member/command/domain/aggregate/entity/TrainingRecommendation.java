package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "training_recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TrainingRecommendation {

  @Id
  @Column(name = "training_recommendation_id")
  private Long trainingRecommendationId;

  @Column(name = "employee_identification_number", nullable = false, length = 30)
  private String employeeIdentificationNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_id", nullable = false)
  private TrainingProgram trainingProgram;

  @Column(name = "recommendation_reason", columnDefinition = "TEXT", nullable = false)
  private String recommendationReason;

  @Column(
      name = "created_at",
      nullable = false,
      columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;
}
