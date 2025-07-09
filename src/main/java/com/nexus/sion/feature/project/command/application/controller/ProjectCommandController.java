package com.nexus.sion.feature.project.command.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectUpdateRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.application.service.ProjectCommandService;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectCommandController {

  private final ProjectCommandService projectCommandService;

  @PostMapping
  public ResponseEntity<ApiResponse<ProjectRegisterResponse>> registerProject(
      @RequestBody ProjectRegisterRequest request) {
    ProjectRegisterResponse response = projectCommandService.registerProject(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @PutMapping("/{projectCode}")
  public ResponseEntity<ApiResponse<Void>> updateProject(
      @PathVariable String projectCode, @RequestBody ProjectUpdateRequest request) {
    request.setProjectCode(projectCode);
    projectCommandService.updateProject(request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/{projectCode}")
  public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable String projectCode) {
    projectCommandService.deleteProject(projectCode);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/{projectCode}/status/{status}")
  public ResponseEntity<ApiResponse<Void>> updateProjectStatus(
      @PathVariable String projectCode, @PathVariable Project.ProjectStatus status) {
    projectCommandService.updateProjectStatus(projectCode, status);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PostMapping("/{projectCode}/analyze")
  public ResponseEntity<Void> analyzeProject(
          @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable String projectCode, @RequestParam("file") MultipartFile multipartFile) {

    projectCommandService.analyzeProject(projectCode, multipartFile, userDetails.getUsername());
    return ResponseEntity.accepted().build();
  }
}
