package com.nexus.sion.feature.project.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainRequest {
  @NotBlank String name;
}
