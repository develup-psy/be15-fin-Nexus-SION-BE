package com.nexus.sion.feature.squad.command.application.service;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import jakarta.validation.Valid;

public interface SquadCommandService {
  void registerManualSquad(SquadRegisterRequest request);

  void updateManualSquad(SquadUpdateRequest request);

  void deleteSquad(String squadCode);

    void recommendSquad(@Valid SquadRecommendationRequest request);
}
