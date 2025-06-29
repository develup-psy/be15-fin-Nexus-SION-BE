package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreSetRequset;

public interface InitialScoreCommandService {
    void setInitialScores(InitialScoreSetRequset requset);
}
