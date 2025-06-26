package com.nexus.sion.feature.project.query.service;

import org.springframework.stereotype.Service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;
import com.nexus.sion.feature.project.query.repository.ProjectQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectQueryServiceImpl implements ProjectQueryService {

  private final ProjectQueryRepository projectQueryRepository;

  @Override
  public PageResponse<ProjectListResponse> findProjects(ProjectListRequest request) {
    return projectQueryRepository.findProjects(request);
  }
}
