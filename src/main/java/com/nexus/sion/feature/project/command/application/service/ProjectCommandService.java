package com.nexus.sion.feature.project.command.application.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectUpdateRequest;
import com.nexus.sion.feature.project.command.application.dto.request.SquadReplacementRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;

public interface ProjectCommandService {
  ProjectRegisterResponse registerProject(ProjectRegisterRequest request);

  void updateProject(ProjectUpdateRequest request);

  void deleteProject(String projectCode);

  void updateProjectStatus(String projectCode, Project.ProjectStatus status);

  Map<String, Long> findProjectAndJobIdMap(String projectId);

  void analyzeProject(
      String projectId, MultipartFile multipartFile, String employeeIdentificationNumber);

  void replaceMember(SquadReplacementRequest request);

  void updateProjectBudget(String projectId, BigDecimal budget);
}
