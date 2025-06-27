package com.nexus.sion.feature.squad.query.service;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResultResponse;
import com.nexus.sion.feature.squad.query.repository.SquadQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SquadQueryServiceImpl implements SquadQueryService {

  private final SquadQueryRepository squadQueryRepository;

  @Override
  public SquadListResultResponse findSquads(SquadListRequest request) {
    SquadListResultResponse result = squadQueryRepository.findSquads(request);

    if (result == null || result.getContent().isEmpty()) {
      throw new BusinessException(ErrorCode.PROJECT_SQUAD_NOT_FOUND);
    }

    return result;
  }

  @Override
  public SquadDetailResponse getSquadDetailByCode(String squadCode) {
    SquadDetailResponse response = squadQueryRepository.findSquadDetailByCode(squadCode);

    if (response == null) {
      throw new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND);
    }

    return response;
  }
}
