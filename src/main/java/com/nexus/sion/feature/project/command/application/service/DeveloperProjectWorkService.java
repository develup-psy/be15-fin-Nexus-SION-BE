package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryRequestDto;

public interface DeveloperProjectWorkService {
  Long requestWork(WorkHistoryRequestDto dto);

  void approve(Long id, String adminId);

  void reject(Long id, String adminId);
}
