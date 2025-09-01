package com.nexus.sion.feature.squad.command.application.service;

import jakarta.validation.Valid;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.response.SquadRecommendationResponse;

public interface SquadRecommendationService {

  SquadRecommendationResponse recommendSquad(@Valid SquadRecommendationRequest request);

}
