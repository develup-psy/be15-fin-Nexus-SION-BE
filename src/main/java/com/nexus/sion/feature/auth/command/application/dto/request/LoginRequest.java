package com.nexus.sion.feature.auth.command.application.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
  private String employeeIdentificationNumber;
  private String password;
}
