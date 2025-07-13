package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryAddRequestDto;

public interface DeveloperProjectWorkService {
  void approve(Long id, String adminId);

  void reject(Long id, String adminId, String reason);

  void addHistories(Long workId, WorkHistoryAddRequestDto dto);
}
