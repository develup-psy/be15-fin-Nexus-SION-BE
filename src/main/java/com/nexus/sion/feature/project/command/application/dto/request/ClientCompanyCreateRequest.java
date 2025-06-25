package com.nexus.sion.feature.project.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientCompanyCreateRequest {
    @NotBlank String companyName;
    @NotBlank String domainName;
    String contactPerson;
    String email;
    String contactNumber;
}
