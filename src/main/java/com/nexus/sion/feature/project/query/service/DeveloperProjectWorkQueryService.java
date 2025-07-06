package com.nexus.sion.feature.project.query.service;

import java.util.List;

import com.nexus.sion.feature.project.query.dto.response.WorkRequestQueryDto;

public interface DeveloperProjectWorkQueryService {
  List<WorkRequestQueryDto> getAllRequests();
}
