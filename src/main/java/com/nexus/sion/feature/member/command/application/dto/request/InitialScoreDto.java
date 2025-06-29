package com.nexus.sion.feature.member.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitialScoreDto {
  @NotBlank Integer minYears;
  Integer maxYears;
  @NotBlank Integer score;
}
