package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.feature.project.query.dto.response.JobRequirement;
import com.nexus.sion.feature.project.query.dto.response.ProjectForSquadResponse;
import com.nexus.sion.feature.project.query.mapper.ProjectQueryMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectDetailResponse;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;
import com.nexus.sion.feature.project.query.repository.ProjectQueryRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectQueryServiceImpl implements ProjectQueryService {

  private final ProjectQueryRepository projectQueryRepository;
  private final ProjectQueryMapper projectQueryMapper;

  @Override
  public PageResponse<ProjectListResponse> findProjects(ProjectListRequest request) {
    return projectQueryRepository.findProjects(request);
  }

  @Override
  public ProjectDetailResponse getProjectDetail(String projectCode) {
    return projectQueryRepository.getProjectDetail(projectCode);
  }

  @Override
  public ProjectForSquadResponse getProjectInfoForSquad(String projectCode) {
    System.out.println(projectCode);
    ProjectForSquadResponse response = projectQueryMapper.findProjectInfo(projectCode);
    if (response == null) {
      throw new EntityNotFoundException("Project not found: " + projectCode);
    }

    List<JobRequirement> requirements = projectQueryMapper.findJobRequirements(projectCode);
    response.setJobRequirements(requirements);
    return response;
  }
}
