package com.nexus.sion.feature.member.command.application.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import lombok.*;

@Getter
@Setter
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

  @NotNull(message = "PDF 파일은 필수입니다.")
  private MultipartFile pdfFileUrl;
}
