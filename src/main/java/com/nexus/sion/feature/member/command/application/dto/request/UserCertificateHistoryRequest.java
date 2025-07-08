package com.nexus.sion.feature.member.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCertificateHistoryRequest {

    @NotBlank(message = "자격증명은 필수입니다.")
    private String certificateName;

    @NotBlank(message = "발급기관은 필수입니다.")
    private String issuingOrganization;

    @NotNull(message = "취득일자는 필수입니다.")
    private LocalDate issueDate;

    @NotBlank(message = "PDF 파일 URL은 필수입니다.")
    private String pdfFileUrl;
}