package com.nexus.sion.feature.evaluation.command.application.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.evaluation.command.application.dto.request.ProjectEvaluationRequest;
import com.nexus.sion.feature.evaluation.command.application.service.ProjectEvaluationCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectEvaluationCommandController {

  private final ProjectEvaluationCommandService projectEvaluationService;

  @PostMapping("/evaluate")
  public ApiResponse<Void> evaluate(@RequestBody ProjectEvaluationRequest request) {
    projectEvaluationService.evaluateProject(request);
    return ApiResponse.success(null);
  }
}
