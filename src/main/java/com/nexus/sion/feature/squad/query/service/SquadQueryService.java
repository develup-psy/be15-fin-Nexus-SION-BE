package com.nexus.sion.feature.squad.query.service;

import java.util.List;
import java.util.Map;

import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadCandidateResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResultResponse;

public interface SquadQueryService {

  // 프로젝트별 스쿼드 목록 조회
  SquadListResultResponse findSquads(SquadListRequest request);

  // 스쿼드 상세 조회
  SquadDetailResponse getSquadDetailByCode(String squadCode);

  // 확정된 스쿼드가 있으면 상세, 없으면 목록 조회
  Object findSquadsOrConfirmed(SquadListRequest request);

  // 해당 프로젝트에 확정된 스쿼드가 있는지 확인
  boolean hasConfirmedSquad(String projectCode);

  // 해당 프로젝트의 확정된 스쿼드 상세 조회
  SquadDetailResponse getConfirmedSquadByProjectCode(String projectCode);

  SquadCandidateResponse findCandidatesByRoles(String projectId);

  Map<String, Integer> findRequiredMemberCountByRoles(String projectId);
}
