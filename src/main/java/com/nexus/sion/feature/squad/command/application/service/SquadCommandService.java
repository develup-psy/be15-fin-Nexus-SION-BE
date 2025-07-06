package com.nexus.sion.feature.squad.command.application.service;

import jakarta.validation.Valid;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;

public interface SquadCommandService {
  void registerManualSquad(SquadRegisterRequest request);

  void updateManualSquad(SquadUpdateRequest request);

  void deleteSquad(String squadCode);

  void recommendSquad(@Valid SquadRecommendationRequest request);

  void confirmSquad(String squadCode);
}
