package com.nexus.sion.feature.techstack.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechStackRequest {
  @NotBlank String techStackName;
}
