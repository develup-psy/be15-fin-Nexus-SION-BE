package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreDto;
import com.nexus.sion.feature.member.query.repository.InitialScoreQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InitialScoreQueryServiceImpl implements InitialScoreQueryService {

    private final InitialScoreQueryRepository initialScoreQueryRepository;

    @Override
    public List<InitialScoreDto> getInitialScores() {
        return initialScoreQueryRepository.getAllScores();
    }
}
