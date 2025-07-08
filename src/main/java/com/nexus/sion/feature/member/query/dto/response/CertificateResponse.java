package com.nexus.sion.feature.member.query.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateResponse {

  private String certificateName;
  private int score;
}
