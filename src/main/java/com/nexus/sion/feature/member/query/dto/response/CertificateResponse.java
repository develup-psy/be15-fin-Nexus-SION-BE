package com.nexus.sion.feature.member.query.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateResponse {

    private String certificateName;
    private int score;
}
