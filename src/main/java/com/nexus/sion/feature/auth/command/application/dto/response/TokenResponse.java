package com.nexus.sion.feature.auth.command.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Schema(description = "JWT 토큰 응답 DTO")
@RequiredArgsConstructor
@AllArgsConstructor
@Setter
public class TokenResponse {
  @Schema(description = "Access Token")
  private String accessToken;

  @Schema(description = "Refresh Token")
  private String refreshToken;
}
