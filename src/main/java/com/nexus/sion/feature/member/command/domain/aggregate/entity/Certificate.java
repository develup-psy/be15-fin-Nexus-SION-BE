package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "certificate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Certificate {

  @Id
  @Column(name = "certificate_name", length = 30, nullable = false)
  private String certificateName;

  @Column(name = "score", nullable = false)
  private Integer score = 0;

  @Column(name = "issuing_organization", length = 30)
  private String issuingOrganization;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public void update(int score, String issuingOrganization) {
    this.score = score;
    this.issuingOrganization = issuingOrganization;
    this.updatedAt = LocalDateTime.now();
  }
}
