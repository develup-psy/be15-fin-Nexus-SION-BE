package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ProjectCommandService {
  ProjectRegisterResponse registerProject(ProjectRegisterRequest request);

  void updateProject(ProjectRegisterRequest request);

  void deleteProject(String projectCode);

  void updateProjectStatus(String projectCode, Project.ProjectStatus status);

    Map<String, Long> findProjectAndJobIdMap(String projectId);

  void analyzeProject(String projectId, MultipartFile multipartFile);
}
