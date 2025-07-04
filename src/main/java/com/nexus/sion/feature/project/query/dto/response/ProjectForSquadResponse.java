package com.nexus.sion.feature.project.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    public void setJobRequirements(List<JobRequirement> requirements) {
        this.jobRequirements = requirements;
    }
}
