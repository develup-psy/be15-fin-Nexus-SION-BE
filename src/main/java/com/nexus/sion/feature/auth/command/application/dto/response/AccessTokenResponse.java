package com.nexus.sion.feature.auth.command.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Schema(description = "JWT 액세스 토큰 응답 DTO")
@RequiredArgsConstructor
@AllArgsConstructor
@Setter
public class AccessTokenResponse {
  @Schema(description = "Access Token")
  private String accessToken;
}
