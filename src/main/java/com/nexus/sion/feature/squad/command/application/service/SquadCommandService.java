package com.nexus.sion.feature.squad.command.application.service;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;

public interface SquadCommandService {
  void registerManualSquad(SquadRegisterRequest request);
  void updateManualSquad(SquadUpdateRequest request);

}
