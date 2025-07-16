package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.*;

@Entity
@Table(name = "training_recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TrainingRecommendation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "training_recommendation_id")
  private Long trainingRecommendationId;

  @Column(name = "training_name", nullable = false, length = 30)
  private String trainingName;

  @Column(name = "training_description", nullable = false, length = 255)
  private String trainingDescription;

  @Column(name = "training_category", nullable = false, length = 30)
  private String trainingCategory;

  @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
  private String imageUrl;

  @Column(name = "video_url", nullable = false, columnDefinition = "TEXT")
  private String videoUrl;

  @Column(name = "employee_identification_number", nullable = false, length = 30)
  private String employeeIdentificationNumber;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
