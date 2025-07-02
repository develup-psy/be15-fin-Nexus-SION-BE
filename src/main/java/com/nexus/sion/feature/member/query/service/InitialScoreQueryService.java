package com.nexus.sion.feature.member.query.service;

import java.util.List;

import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreDto;

public interface InitialScoreQueryService {

  List<InitialScoreDto> getInitialScores();
}
