package com.nexus.sion.feature.squad.command.application.dto.internal;

import java.util.List;

import com.nexus.sion.feature.squad.command.domain.aggregate.enums.EvaluationWeights;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SquadEvaluationResult {

  private final List<DeveloperSummary> members;

  private final double duration;

  private final double cost;

  private final double techScore;

  private final double domainScore;

  private final double finalScore;

  private final EvaluationWeights weights;

  private final String recommendationType; // 예: "A", "B", "C" ...
}
