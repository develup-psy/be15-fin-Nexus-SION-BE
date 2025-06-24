package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.DomainRequest;

public interface DomainCommandService {
  boolean registerDomain(DomainRequest request);
}
