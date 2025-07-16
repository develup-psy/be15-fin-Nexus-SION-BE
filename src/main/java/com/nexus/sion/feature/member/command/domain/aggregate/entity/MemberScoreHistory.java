package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "member_score_history")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberScoreHistory extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "score_history_id")
  private Long id;

  @Column(name = "employee_identification_number", nullable = false)
  private String employeeIdentificationNumber;

  @Column(name = "total_tech_stack_scores", nullable = false)
  private int totalTechStackScores;

  @Column(name = "total_certificate_scores", nullable = false)
  private int totalCertificateScores;

  public static MemberScoreHistory initial(String employeeId) {
    return MemberScoreHistory.builder()
        .employeeIdentificationNumber(employeeId)
        .totalTechStackScores(0)
        .totalCertificateScores(0)
        .build();
  }
}
