package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;

public interface ProjectCommandService {
  ProjectRegisterResponse registerProject(ProjectRegisterRequest request);

  void updateProject(ProjectRegisterRequest request);

  void deleteProject(String projectCode);

  void updateProjectStatus(String projectCode, Project.ProjectStatus status);
}
