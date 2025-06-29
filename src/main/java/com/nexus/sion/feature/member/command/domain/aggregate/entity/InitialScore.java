package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import com.nexus.sion.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "initial_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InitialScore extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "min_years", nullable = false)
  private Integer minYears;

  @Column(name = "max_years")
  private Integer maxYears;

  @Column(nullable = false)
  private Integer score;
}
