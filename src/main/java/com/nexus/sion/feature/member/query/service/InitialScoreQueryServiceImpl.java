package com.nexus.sion.feature.member.query.service;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.InitialScoreResponseDto;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.query.repository.InitialScoreQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InitialScoreQueryServiceImpl implements InitialScoreQueryService {

  private final InitialScoreQueryRepository initialScoreQueryRepository;

  @Override
  public List<InitialScoreResponseDto> getInitialScores() {
    return initialScoreQueryRepository.getAllScores();
  }
}
