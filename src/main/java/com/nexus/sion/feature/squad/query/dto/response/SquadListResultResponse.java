package com.nexus.sion.feature.squad.query.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SquadListResultResponse {
  private List<SquadListResponse> content;
  private int page;
  private int size;
  private long totalCount;
}
