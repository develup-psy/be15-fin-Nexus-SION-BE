package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;

public interface ProjectQueryService {
  PageResponse<ProjectListResponse> findProjects(ProjectListRequest request);
}
