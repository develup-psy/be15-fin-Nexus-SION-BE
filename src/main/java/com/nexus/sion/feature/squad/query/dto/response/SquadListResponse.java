package com.nexus.sion.feature.squad.query.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SquadListResponse {
  private String squadCode;
  private String squadName;
  private boolean isAiRecommended;
  private List<MemberInfo> members;
  private String estimatedPeriod;
  private String estimatedCost;

  public record MemberInfo(String name, String job) {}
}
