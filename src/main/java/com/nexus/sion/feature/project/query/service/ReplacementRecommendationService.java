package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;

import java.util.List;

public interface ReplacementRecommendationService {
    List<DeveloperSummary> recommendCandidates(String projectCode, String employeeId);
}
