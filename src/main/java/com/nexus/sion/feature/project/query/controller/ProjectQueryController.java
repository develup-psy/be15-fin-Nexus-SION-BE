package com.nexus.sion.feature.project.query.controller;

import com.nexus.sion.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;
import com.nexus.sion.feature.project.query.service.ProjectQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/projects")
@RequiredArgsConstructor
public class ProjectQueryController {

  private final ProjectQueryService projectQueryService;

  @PostMapping("/list")
  public ApiResponse<PageResponse<ProjectListResponse>> searchProjects(@RequestBody ProjectListRequest request) {
    PageResponse<ProjectListResponse> result = projectQueryService.findProjects(request);
    return ApiResponse.success(result);
  }
}
