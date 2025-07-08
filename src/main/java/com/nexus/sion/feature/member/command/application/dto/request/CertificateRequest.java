package com.nexus.sion.feature.member.command.application.dto.request;

import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateRequest {

  @NotBlank(message = "자격증명은 필수입니다.")
  private String certificateName;

  @NotNull(message = "점수는 필수 항목입니다.")
  private int score;
}
