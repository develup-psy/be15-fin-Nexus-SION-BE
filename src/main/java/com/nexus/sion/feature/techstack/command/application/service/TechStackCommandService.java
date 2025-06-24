package com.nexus.sion.feature.techstack.command.application.service;

import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackCreateRequest;

public interface TechStackCommandService {
  void registerTechStack(TechStackCreateRequest request);
}
