package com.nexus.sion.feature.squad.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.squad.query.dto.response.SquadCommentResponse;
import com.nexus.sion.feature.squad.query.repository.SquadCommentQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SquadCommentQueryServiceImpl implements SquadCommentQueryService {

  private final SquadCommentQueryRepository squadCommentQueryRepository;

  @Override
  public List<SquadCommentResponse> findCommentsBySquadCode(String squadCode) {
    return squadCommentQueryRepository.findBySquadCode(squadCode);
  }
}
