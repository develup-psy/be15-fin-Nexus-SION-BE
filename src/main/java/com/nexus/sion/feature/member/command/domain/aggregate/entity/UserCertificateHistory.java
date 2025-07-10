package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.CertificateStatus;

import lombok.*;

@Entity
@Table(name = "user_certificate_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCertificateHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_certificate_history_id")
  private Long id;

  @Column(name = "issue_date", nullable = false)
  private LocalDate issueDate;

  @Column(name = "pdf_file_url", length = 255, nullable = false)
  private String pdfFileUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "certificate_status", nullable = false)
  private CertificateStatus certificateStatus;

  @Column(name = "rejected_reason", columnDefinition = "TEXT")
  private String rejectedReason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "employee_identification_number", length = 30, nullable = false)
  private String employeeIdentificationNumber;

  @Column(name = "certificate_name", length = 30, nullable = false)
  private String certificateName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "certificate_name", referencedColumnName = "certificate_name", insertable = false, updatable = false)
  private Certificate certificate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_identification_number", referencedColumnName = "employee_identification_number", insertable = false, updatable = false)
  private Member member;

  public void approve() {
    this.certificateStatus = CertificateStatus.APPROVED;
    this.updatedAt = LocalDateTime.now();
  }

  public void reject(String reason) {
    this.certificateStatus = CertificateStatus.REJECTED;
    this.rejectedReason = reason;
    this.updatedAt = LocalDateTime.now();
  }
}
