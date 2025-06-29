package com.nexus.sion.feature.squad.command.domain.service;

import com.nexus.sion.feature.squad.command.application.dto.internal.CandidateSummary;
import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SquadEvaluatorImpl  {

    private static final double DEFAULT_MAN_MONTH_PER_FP = 0.3; // 인월/FP 기준
    private static final int DEFAULT_TOTAL_FP = 160; // 전체 기능점수 기준

    public List<EvaluatedSquad> evaluateAll(List<Map<String, List<CandidateSummary>>> squadCombinations) {
        List<EvaluatedSquad> results = new ArrayList<>();

        for (Map<String, List<CandidateSummary>> squad : squadCombinations) {
            List<CandidateSummary> allMembers = squad.values().stream()
                    .flatMap(List::stream)
                    .toList();

            // 평균 점수 계산
            int totalTechScore = allMembers.stream().mapToInt(CandidateSummary::getTechStackScore).sum();
            double totalDomainScore = allMembers.stream().mapToDouble(CandidateSummary::getDomainRelevance).sum();
            int totalMonthlyCost = allMembers.stream().mapToInt(CandidateSummary::getCostPerMonth).sum();
            double totalProductivity = allMembers.stream().mapToDouble(CandidateSummary::getProductivityFactor).sum();

            int avgTech = totalTechScore / allMembers.size();
            double avgDomain = totalDomainScore / allMembers.size();

            // FP 기준 총 인월 계산
            int totalManMonth = (int) Math.ceil(DEFAULT_TOTAL_FP * DEFAULT_MAN_MONTH_PER_FP);

            // 총 생산성 계수 기준으로 기간 추정
            int estimatedDuration = (int) Math.ceil(totalManMonth / totalProductivity);

            results.add(new EvaluatedSquad(
                    squad,
                    avgTech,
                    avgDomain,
                    totalMonthlyCost,
                    totalManMonth,
                    estimatedDuration
            ));
        }

        return results;
    }
}