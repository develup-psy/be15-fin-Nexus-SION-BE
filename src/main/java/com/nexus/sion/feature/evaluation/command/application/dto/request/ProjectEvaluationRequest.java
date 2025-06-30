package com.nexus.sion.feature.evaluation.command.application.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectEvaluationRequest {
  private String projectCode;
  private List<MemberEvaluationDto> evaluations;

  @Getter
  @Setter
  public static class MemberEvaluationDto {
    private String employeeId;
    private List<TechStackScoreDto> techStacks;
  }

  @Getter
  @Setter
  public static class TechStackScoreDto {
    private String techStackName;
    private int score;
  }
}
