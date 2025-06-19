package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "project_and_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectAndJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "project_and_job_id")
  private Long id;

  @Column(name = "required_number", nullable = false)
  private int requiredNumber;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "project_code", nullable = false, length = 30)
  private String projectCode;

  @Column(name = "job_name", nullable = false, length = 30)
  private String jobName;
}
