package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "developer_project_work")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
  private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

  @Column(name = "approved_by")
  private String approvedBy;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @OneToMany(mappedBy = "developerProjectWork", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DeveloperProjectWorkHistory> histories = new ArrayList<>();

  public DeveloperProjectWork(String employeeIdentificationNumber, String projectCode) {
    this.employeeIdentificationNumber = employeeIdentificationNumber;
    this.projectCode = projectCode;
    this.approvalStatus = ApprovalStatus.PENDING;
  }

  public void addHistory(DeveloperProjectWorkHistory history) {
    histories.add(history);
    history.setDeveloperProjectWork(this);
  }

  public void approve(String adminId) {
    this.approvalStatus = ApprovalStatus.APPROVED;
    this.approvedBy = adminId;
    this.approvedAt = LocalDateTime.now();
  }

  public void reject(String adminId) {
    this.approvalStatus = ApprovalStatus.REJECTED;
    this.approvedBy = adminId;
    this.approvedAt = LocalDateTime.now();
  }

  public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
  }
}
