package com.nexus.sion.feature.squad.command.domain.aggregate.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;

import lombok.*;

@Entity
@Table(name = "squad")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Squad extends BaseTimeEntity {
  // base entity : 생성일자, 수정일자 자동생성 및 업데이트 설정

  @Id
  @Column(name = "squad_code", length = 30)
  private String squadCode;

  @Column(name = "project_code", length = 30, nullable = false)
  private String projectCode;

  @Column(name = "title", length = 30, nullable = false)
  private String title;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "estimated_duration", precision = 5, scale = 2)
  private BigDecimal estimatedDuration;

  @Column(name = "estimated_cost", precision = 12, scale = 2)
  private BigDecimal estimatedCost;

  @Enumerated(EnumType.STRING)
  @Column(name = "origin_type", nullable = false)
  private OriginType originType;

  @Column(name = "recommendation_reason", columnDefinition = "TEXT")
  private String recommendationReason;
}
