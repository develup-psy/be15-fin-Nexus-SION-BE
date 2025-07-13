package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "developer_project_work")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeveloperProjectWork extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "developer_project_work_id")
  private Long id;

  @Column(name = "employee_identification_number", nullable = false)
  private String employeeIdentificationNumber;

  @Column(name = "project_code", nullable = false)
  private String projectCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "approval_status", nullable = false)
  private ApprovalStatus approvalStatus = ApprovalStatus.NOT_REQUESTED;

  @Column(name = "approved_by")
  private String approvedBy;

  @Column(name = "rejected_reason", columnDefinition = "TEXT")
  private String rejectedReason;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  public DeveloperProjectWork(String employeeIdentificationNumber, String projectCode) {
    this.employeeIdentificationNumber = employeeIdentificationNumber;
    this.projectCode = projectCode;
    this.approvalStatus = ApprovalStatus.NOT_REQUESTED;
  }

  public DeveloperProjectWork(
          String employeeIdentificationNumber, String projectCode, ApprovalStatus approvalStatus) {
    this.employeeIdentificationNumber = employeeIdentificationNumber;
    this.projectCode = projectCode;
    this.approvalStatus = approvalStatus != null ? approvalStatus : ApprovalStatus.PENDING;
  }

  public void approve(String adminId) {
    this.approvalStatus = ApprovalStatus.APPROVED;
    this.approvedBy = adminId;
    this.approvedAt = LocalDateTime.now();
    this.rejectedReason = null; // 승인 시 반려 사유 제거
  }

  public void reject(String adminId, String reason) {
    this.approvalStatus = ApprovalStatus.REJECTED;
    this.approvedBy = adminId;
    this.approvedAt = LocalDateTime.now();
    this.rejectedReason = reason;
  }

  public void setApprovalStatus(ApprovalStatus status) {
    this.approvalStatus = status;
  }

  public enum ApprovalStatus {
    NOT_REQUESTED,
    PENDING,
    APPROVED,
    REJECTED
  }
}