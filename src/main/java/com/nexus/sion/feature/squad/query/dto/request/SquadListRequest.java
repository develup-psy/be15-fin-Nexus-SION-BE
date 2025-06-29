package com.nexus.sion.feature.squad.query.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadListRequest {
  private String projectCode;
  private int page = 0;
  private int size = 10;
}
