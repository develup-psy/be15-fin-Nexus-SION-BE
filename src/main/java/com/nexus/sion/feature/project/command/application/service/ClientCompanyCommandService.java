package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;

public interface ClientCompanyCommandService {
  void registerClientCompany(ClientCompanyCreateRequest request);
}
