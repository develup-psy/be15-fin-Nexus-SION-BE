package com.nexus.sion.feature.member.command.application.dto.request;

import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateUpdateRequest {

  @NotBlank(message = "발급기관은 필수입니다.")
  private String issuingOrganization;

  @NotNull(message = "점수는 필수 항목입니다.")
  private Integer score;
}
