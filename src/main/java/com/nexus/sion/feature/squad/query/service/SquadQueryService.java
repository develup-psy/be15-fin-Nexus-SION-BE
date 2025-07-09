package com.nexus.sion.feature.squad.query.service;

import java.util.Map;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadCandidateResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;

public interface SquadQueryService {

  PageResponse<SquadListResponse> getSquads(SquadListRequest request);

  SquadCandidateResponse findCandidatesByRoles(String projectId);

  Map<String, Integer> findRequiredMemberCountByRoles(String projectId);

  SquadDetailResponse getSquadDetailByCode(String squadCode);
}
