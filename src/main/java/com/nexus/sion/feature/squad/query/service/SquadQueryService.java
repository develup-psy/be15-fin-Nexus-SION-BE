package com.nexus.sion.feature.squad.query.service;

import java.util.List;

import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;

public interface SquadQueryService {

  // 프로젝트별 스쿼드 목록 조회
  List<SquadListResponse> findSquads(SquadListRequest request);

  // 스쿼드 상세 조회
  SquadDetailResponse getSquadDetailByCode(String squadCode);
}
