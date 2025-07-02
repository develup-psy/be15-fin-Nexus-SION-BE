package com.nexus.sion.feature.member.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitialScoreResponseDto {
  long id;
  Integer minYears;
  Integer maxYears;
  Integer score;
}
