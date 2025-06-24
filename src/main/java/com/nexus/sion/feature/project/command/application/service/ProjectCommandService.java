package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;

public interface ProjectCommandService {
  ProjectRegisterResponse registerProject(ProjectRegisterRequest request);

  void updateProject(ProjectRegisterRequest request);
}
