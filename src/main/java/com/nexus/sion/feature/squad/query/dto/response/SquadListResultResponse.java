package com.nexus.sion.feature.squad.query.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class SquadListResultResponse implements SquadResponse {
  private List<SquadListResponse> content;
  private int page;
  private int size;
  private int totalPage;
  private long totalCount;
}
