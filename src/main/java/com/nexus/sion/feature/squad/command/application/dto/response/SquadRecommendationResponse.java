package com.nexus.sion.feature.squad.command.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadRecommendationResponse {
  private String squadCode;
  private String projectCode;
  private String title;
  private String description;
  private BigDecimal estimatedCost;
  private BigDecimal estimatedDuration;
  private String recommendationReason;
  private List<MemberInfo> members;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MemberInfo {
    private String employeeIdentificationNumber;
    private String jobName;
    private Boolean isLeader;
    private Integer totalSkillScore;
  }
}
