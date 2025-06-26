package com.nexus.sion.feature.project.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientCompanyCreateRequest {
  @NotBlank
  @Size(min = 2, message = "회사명은 최소 2글자 이상이어야 합니다.")
  String companyName;

  @NotBlank String domainName;
  String contactPerson;
  String email;
  String contactNumber;
}
