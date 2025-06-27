package com.nexus.sion.feature.squad.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SquadListResultResponse {
  private List<SquadListResponse> content;
  private int page;
  private int size;
  private long totalCount;
}
