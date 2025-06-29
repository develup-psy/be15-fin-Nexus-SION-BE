package com.nexus.sion.feature.squad.command.application.dto.internal;

import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluatedSquad {
    private Map<String, List<CandidateSummary>> squad;
    private int averageTechStackScore;
    private double averageDomainRelevance;
    private int totalMonthlyCost;        // 단가 총합
    private int estimatedManMonth;       // 총 인월
    private int estimatedDuration;       // 월 단위 기간
}
