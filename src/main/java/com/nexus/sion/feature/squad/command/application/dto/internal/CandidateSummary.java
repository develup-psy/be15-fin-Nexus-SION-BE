package com.nexus.sion.feature.squad.command.application.dto.internal;

import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSummary {
    private String memberId;
    private String name;
    private String jobName;
    private int techStackScore;
    private double domainRelevance;   // 0.0 ~ 1.0
    private int costPerMonth;         // 단가 (만원 단위)
    private double productivityFactor; // 생산성 계수 (예: 1.0, 1.2 등)

}
