package com.nexus.sion.feature.squad.query.service;

import java.util.List;

import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;

public interface SquadQueryService {
    List<SquadListResponse> findSquads(SquadListRequest request);
}
