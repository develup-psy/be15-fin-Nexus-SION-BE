package com.nexus.sion.feature.project.query.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.query.dto.response.WorkRequestQueryDto;
import com.nexus.sion.feature.project.query.service.DeveloperProjectWorkQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/dev-project-works")
@RequiredArgsConstructor
public class DeveloperProjectWorkQueryController {

  private final DeveloperProjectWorkQueryService service;

  @GetMapping
  public ApiResponse<List<WorkRequestQueryDto>> getAllRequests() {
    return ApiResponse.success(service.getAllRequests());
  }
}
