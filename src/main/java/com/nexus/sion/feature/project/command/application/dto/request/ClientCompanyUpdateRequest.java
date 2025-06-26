package com.nexus.sion.feature.project.command.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email;
    @Pattern(
            regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
            message = "유효한 휴대전화 번호 형식이 아닙니다. 예: 010-1234-5678"
    )
    String contactNumber;
}
