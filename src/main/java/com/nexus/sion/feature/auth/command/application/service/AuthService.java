package com.nexus.sion.feature.auth.command.application.service;

import com.nexus.sion.feature.auth.command.application.dto.response.TokenResponse;

public interface AuthService {

  TokenResponse testLogin();
}
