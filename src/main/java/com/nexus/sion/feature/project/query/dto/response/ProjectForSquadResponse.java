package com.nexus.sion.feature.project.query.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectForSquadResponse {
    private String projectCode;
    private Long budgetLimit;
    private Double durationLimit;
    private Double estimatedDuration;
    private Long estimatedCost;
    private List<JobRequirement> jobRequirements;
    private Double totalEffort;

  public void setJobRequirements(List<JobRequirement> requirements) {
    this.jobRequirements = requirements;
  }
}
