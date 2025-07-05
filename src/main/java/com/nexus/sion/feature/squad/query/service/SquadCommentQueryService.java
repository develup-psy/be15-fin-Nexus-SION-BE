package com.nexus.sion.feature.squad.query.service;

import java.util.List;

import com.nexus.sion.feature.squad.query.dto.response.SquadCommentResponse;

public interface SquadCommentQueryService {
  List<SquadCommentResponse> findCommentsBySquadCode(String squadCode);
}
