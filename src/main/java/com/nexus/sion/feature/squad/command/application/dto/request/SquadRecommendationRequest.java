package com.nexus.sion.feature.squad.command.application.dto.request;

import jakarta.validation.constraints.NotNull;

import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SquadRecommendationRequest {

  @NotNull(message = "projectId는 필수입니다.")
  private String projectId;

  @NotNull(message = "추천 기준(criteria)은 필수입니다.")
  private RecommendationCriteria criteria;
}
