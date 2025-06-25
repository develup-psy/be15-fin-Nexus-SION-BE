package com.nexus.sion.feature.project.query.controller;

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
  public PageResponse<ProjectListResponse> searchProjects(@RequestBody ProjectListRequest request) {
    return projectQueryService.findProjects(request);
  }
}
