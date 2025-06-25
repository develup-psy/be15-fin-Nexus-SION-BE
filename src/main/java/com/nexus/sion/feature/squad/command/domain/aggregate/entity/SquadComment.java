package com.nexus.sion.feature.squad.command.domain.aggregate.entity;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "squad_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SquadComment extends BaseTimeEntity {
  // base entity : 생성일자, 수정일자 자동생성 및 업데이트 설정

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_id")
  private Long id;

  @Column(name = "squad_code", length = 30, nullable = false)
  private String squadCode;

  @Column(name = "employee_identification_number", length = 30, nullable = false)
  private String employeeIdentificationNumber;

  @Column(name = "content", columnDefinition = "TEXT", nullable = false)
  private String content;
}
