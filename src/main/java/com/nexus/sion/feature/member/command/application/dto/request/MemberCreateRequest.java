package com.nexus.sion.feature.member.command.application.dto.request;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberCreateRequest {
  private String employeeIdentificationNumber;
  private String employeeName;
  private String password;
  private String phoneNumber;
  private String email;
}
