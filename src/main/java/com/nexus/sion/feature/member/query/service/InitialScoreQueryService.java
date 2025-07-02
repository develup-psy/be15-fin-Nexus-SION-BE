package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreDto;

import java.util.List;

public interface InitialScoreQueryService {

    List<InitialScoreDto> getInitialScores();

}
