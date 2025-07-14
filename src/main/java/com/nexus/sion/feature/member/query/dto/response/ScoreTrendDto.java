package com.nexus.sion.feature.member.query.dto.response;

public record ScoreTrendDto(
    String month, // "2025-01"
    String techStackName, // 기술스택 이름 (없으면 null)
    int score // 점수
    ) {}
