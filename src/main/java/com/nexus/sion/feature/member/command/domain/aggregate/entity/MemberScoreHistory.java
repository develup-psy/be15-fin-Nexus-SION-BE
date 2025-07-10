package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import com.nexus.sion.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
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
}
