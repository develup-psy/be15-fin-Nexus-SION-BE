package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.RelatedTable;

import lombok.*;

@Entity
@Table(name = "developer_tech_stack_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeveloperTechStackHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "developer_tech_stack_history_id")
  private Long id;

  @Column(name = "developer_tech_stack_id", nullable = false)
  private Long developerTechStackId;

  @Column(name = "added_score", nullable = false)
  private int addedScore;

  @Column(name = "cumulative_score", nullable = false)
  private int cumulativeScore;

  @Enumerated(EnumType.STRING)
  @Column(name = "related_table", nullable = false, length = 30)
  private RelatedTable relatedTable;

  @Column(name = "related_id", nullable = false)
  private Long relatedId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
