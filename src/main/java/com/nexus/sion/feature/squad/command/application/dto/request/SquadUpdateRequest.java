package com.nexus.sion.feature.squad.command.application.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadUpdateRequest {

  @NotBlank private String squadCode; // 수정할 스쿼드 식별자

  @NotBlank private String title;

  private String description;

  @NotNull private BigDecimal estimatedCost;

  @NotNull private BigDecimal estimatedDuration;

  @NotEmpty private List<Developer> developers;
}
