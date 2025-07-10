package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDateTime;

import com.nexus.sion.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.RelatedTable;

import lombok.*;

@Entity
@Table(name = "developer_tech_stack_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeveloperTechStackHistory extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "developer_tech_stack_history_id")
  private Long id;

  @Column(name = "developer_tech_stack_id", nullable = false)
  private Long developerTechStackId;

  @Column(name = "project_code", nullable = false)
  private String projectCode;

  @Column(name = "added_score", nullable = false)
  private Integer addedScore;
}
