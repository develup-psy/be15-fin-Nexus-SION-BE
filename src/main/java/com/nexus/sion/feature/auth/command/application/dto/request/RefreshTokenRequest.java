package com.nexus.sion.feature.auth.command.application.dto.request;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class RefreshTokenRequest {
  private String refreshToken;
}
