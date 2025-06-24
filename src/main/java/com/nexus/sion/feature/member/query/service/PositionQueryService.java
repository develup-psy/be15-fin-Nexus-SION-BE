package com.nexus.sion.feature.member.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.query.dto.response.PositionResponse;
import com.nexus.sion.feature.member.query.repository.PositionQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PositionQueryService {

  private final PositionQueryRepository positionQueryRepository;

  public List<PositionResponse> getPositions() {
    return positionQueryRepository.findAllPositions();
  }
}
