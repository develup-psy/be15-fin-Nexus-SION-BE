package com.nexus.sion.feature.member.command.application.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserCreateRequest {
  private final String employeeIdentificationNumber;
  private final String employeeName;
  private final String password;
  private final String phoneNumber;
  private final String email;
}
