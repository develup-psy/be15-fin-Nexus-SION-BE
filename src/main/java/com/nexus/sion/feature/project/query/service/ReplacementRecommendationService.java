package com.nexus.sion.feature.project.query.service;

import java.util.List;

import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;

public interface ReplacementRecommendationService {
  List<DeveloperSummary> recommendCandidates(String projectCode, String employeeId);
}
