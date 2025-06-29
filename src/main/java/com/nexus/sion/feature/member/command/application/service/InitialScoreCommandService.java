package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreSetRequest;

public interface InitialScoreCommandService {
  void setInitialScores(InitialScoreSetRequest request);
}
