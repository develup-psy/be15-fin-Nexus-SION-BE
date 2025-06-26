package com.nexus.sion.feature.project.command.application.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCompanyUpdateRequest {
    String companyName;
    String domainName;
    String contactPerson;
    String email;
    String contactNumber;
}
