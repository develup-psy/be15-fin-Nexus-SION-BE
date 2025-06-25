package com.nexus.sion.feature.squad.command.application.service;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;

public interface SquadCommandService {
  void registerManualSquad(SquadRegisterRequest request);
}
