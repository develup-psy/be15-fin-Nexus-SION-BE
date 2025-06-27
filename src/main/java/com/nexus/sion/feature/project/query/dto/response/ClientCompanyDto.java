package com.nexus.sion.feature.project.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // 또는 @Getter + @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientCompanyDto {
    String clientCode;
    String companyName;
    String domainName;
    String contactPerson;
    String email;
    String contactNumber;
}
