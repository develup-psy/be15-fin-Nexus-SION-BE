package com.nexus.sion.feature.squad.command.domain.service;

import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SquadCandidateFilter {
    static final double TOP_RATIO = 0.3;

    public Map<String, List<DeveloperSummary>> filterTopNByCriteria(
            Map<String, List<DeveloperSummary>> candidates, RecommendationCriteria criteria) {


        Comparator<DeveloperSummary> comparator = switch (criteria) {
            case TECH_STACK -> Comparator.comparing(DeveloperSummary::getAvgTechScore);
            case DOMAIN_MATCH -> Comparator.comparing(DeveloperSummary::getDomainCount);
            case COST_OPTIMIZED -> Comparator.comparing(DeveloperSummary::getMonthlyUnitPrice);
            case TIME_OPTIMIZED -> Comparator.comparing(DeveloperSummary::getProductivity);
            case BALANCED -> Comparator.comparing(DeveloperSummary::getWeight);
        };

        return candidates.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> {
                                    List<DeveloperSummary> list = entry.getValue();
                                    list.sort(comparator);
                                    int topN = (int) Math.ceil(list.size() * TOP_RATIO);
                                    return list.subList(0, Math.min(topN, list.size()));
                                }));
    }
}
