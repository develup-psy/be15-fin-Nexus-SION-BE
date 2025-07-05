package com.nexus.sion.feature.squad.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Developer {
  @NotBlank private String employeeId;

  @NotNull private Long projectAndJobId;

  @NotNull private Boolean isLeader = false;
}
