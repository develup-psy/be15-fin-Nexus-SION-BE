package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyUpdateRequest;
import jakarta.validation.Valid;

public interface ClientCompanyCommandService {
  void registerClientCompany(ClientCompanyCreateRequest request);

  void updateClientCompany(@Valid ClientCompanyUpdateRequest request, String clientCode);
}
