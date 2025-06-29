package com.nexus.sion.feature.member.command.application.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitialScoreDto {
  @NotNull Integer minYears;
  Integer maxYears;
  @NotNull Integer score;
}
