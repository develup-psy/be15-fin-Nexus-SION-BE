package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Project {

  @Id
  @Column(name = "project_code", length = 30)
  private String projectCode;

  @Column(name = "name", nullable = false, length = 30)
  private String name;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "title", nullable = false, length = 30)
  private String title;

  @Column(name = "budget", nullable = false)
  private Long budget;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "expected_end_date", nullable = false)
  private LocalDate expectedEndDate;

  @Column(name = "actual_end_date")
  private LocalDate actualEndDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProjectStatus status;

  @Column(name = "number_of_members")
  private Integer numberOfMembers;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "client_code", nullable = false, length = 30)
  private String clientCode;

  @Column(name = "request_specification_url", length = 255)
  private String requestSpecificationUrl;

  public enum ProjectStatus {
    WAITING,
    IN_PROGRESS,
    COMPLETE,
    INCOMPLETE
  }
}
