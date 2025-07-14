package com.nexus.sion.feature.squad.command.application.dto.request;

import jakarta.validation.constraints.NotNull;

import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadRecommendationRequest {

  @NotNull(message = "projectId는 필수입니다.")
  private String projectId;

  @NotNull(message = "추천 기준(criteria)은 필수입니다.")
  private RecommendationCriteria criteria;
}
