package com.nexus.sion.feature.squad.command.domain.service;

import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SquadSelectorImpl{

    public EvaluatedSquad selectBest(List<EvaluatedSquad> squads, RecommendationCriteria criteria) {
        return squads.stream()
                .max(Comparator.comparingDouble(squad -> calculateScore(squad, criteria)))
                .orElseThrow(() -> new IllegalArgumentException("추천 가능한 스쿼드가 없습니다."));
    }

    private double calculateScore(EvaluatedSquad squad, RecommendationCriteria criteria) {
        // 기준 점수 항목 정규화 (높을수록 좋은 항목은 정규화, 낮을수록 좋은 항목은 역정규화)
        double tech = squad.getAverageTechStackScore();     // 높을수록 좋음
        double domain = squad.getAverageDomainRelevance(); // 높을수록 좋음
        double cost = squad.getTotalMonthlyCost();          // 낮을수록 좋음
        double duration = squad.getEstimatedDuration();     // 낮을수록 좋음

        // 가중치 설정
        double techWeight = 0.25;
        double domainWeight = 0.25;
        double costWeight = 0.25;
        double durationWeight = 0.25;

        switch (criteria) {
            case TECH_STACK -> techWeight = 0.4;
            case DOMAIN_MATCH -> domainWeight = 0.4;
            case COST_OPTIMIZED -> costWeight = 0.4;
            case TIME_OPTIMIZED -> durationWeight = 0.4;
            case BALANCED -> {} // default 균형
        }

        // 정규화 지표 계산 (최대값 기준 정규화는 평가 전 사전 계산해야 정확하나, 여기선 간단화를 위해 직접 반영)
        double costScore = 1000000.0 / (cost + 1);         // 낮을수록 높게 점수
        double durationScore = 100.0 / (duration + 1);     // 낮을수록 높게 점수

        return tech * techWeight +
                domain * domainWeight +
                costScore * costWeight +
                durationScore * durationWeight;
    }
}

