package com.nexus.sion.feature.member.query.service;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.TrainingRecommendationResponse;

public interface TrainingRecommendationQueryService {
  List<TrainingRecommendationResponse> recommendTrainingsFor(String employeeId);
}
