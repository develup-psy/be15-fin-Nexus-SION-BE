package com.nexus.sion.feature.project.command.application.service;

import jakarta.validation.Valid;

import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyUpdateRequest;

public interface ClientCompanyCommandService {
  void registerClientCompany(ClientCompanyCreateRequest request);

  void updateClientCompany(@Valid ClientCompanyUpdateRequest request, String clientCode);

  void deleteClientCompany(String clientCode);
}
