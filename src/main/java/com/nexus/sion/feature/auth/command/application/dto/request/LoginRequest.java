package com.nexus.sion.feature.auth.command.application.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginRequest {
    private final String employeeIdentificationNumber;
    private final String password;
}
