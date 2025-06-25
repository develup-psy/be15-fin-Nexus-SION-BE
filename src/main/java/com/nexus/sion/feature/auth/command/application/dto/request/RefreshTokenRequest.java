package com.nexus.sion.feature.auth.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class RefreshTokenRequest {
  @NotBlank private String refreshToken;
}
