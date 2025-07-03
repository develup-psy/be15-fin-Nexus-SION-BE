package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Project extends BaseTimeEntity {
  // base entity : 생성일자, 수정일자 자동생성 및 업데이트 설정

  @Id
  @Column(name = "project_code", length = 30)
  private String projectCode;

  @Column(name = "domain_name", nullable = false, length = 30)
  private String domainName;

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

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "client_code", nullable = false, length = 30)
  private String clientCode;

  @Column(name = "request_specification_url", length = 255)
  private String requestSpecificationUrl;

  @Builder.Default
  @Column(name = "analysis_status")
  @Enumerated(EnumType.STRING)
  private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

  public enum ProjectStatus {
    WAITING,
    IN_PROGRESS,
    COMPLETE,
    INCOMPLETE
  }

  public enum AnalysisStatus {
    PENDING, // 분석 요청 전
    PROCEEDING, // 분석 진행 중
    COMPLETE, // 분석 완료
    FAILED // 실패
  }
}
