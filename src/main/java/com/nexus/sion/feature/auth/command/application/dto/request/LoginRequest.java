package com.nexus.sion.feature.auth.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
  @NotBlank private String employeeIdentificationNumber;
  @NotBlank private String password;
}
