package com.nexus.sion.feature.project.query.controller;

import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectDetailResponse;
import com.nexus.sion.feature.project.query.dto.response.ProjectForSquadResponse;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;
import com.nexus.sion.feature.project.query.service.ProjectQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/projects")
@RequiredArgsConstructor
public class ProjectQueryController {

  private final ProjectQueryService projectQueryService;

  // 목록 조회
  @PostMapping("/list")
  public ApiResponse<PageResponse<ProjectListResponse>> searchProjects(
      @RequestBody ProjectListRequest request) {
    PageResponse<ProjectListResponse> result = projectQueryService.findProjects(request);
    return ApiResponse.success(result);
  }

  // 상세 조회
  @GetMapping("/list/{projectCode}")
  public ApiResponse<ProjectDetailResponse> getProjectDetail(@PathVariable String projectCode) {
    ProjectDetailResponse result = projectQueryService.getProjectDetail(projectCode);
    return ApiResponse.success(result);
  }

  // 스쿼드용 상세조회
  @GetMapping("/squad/{projectCode}")
  public ApiResponse<ProjectForSquadResponse> getProjectInfoForSquad(
      @PathVariable String projectCode) {
    ProjectForSquadResponse result = projectQueryService.getProjectInfoForSquad(projectCode);
    return ApiResponse.success(result);
  }

  @GetMapping("/member/{employeeId}/details/{projectCode}")
  public ApiResponse<ProjectDetailResponse> getProjectDetailForMember(
      @PathVariable String employeeId, @PathVariable String projectCode) {
    return ApiResponse.success(
        projectQueryService.findProjectDetailByMemberIdAndProjectCode(employeeId, projectCode));
  }

  @GetMapping("/member/{employeeId}/list")
  public ApiResponse<PageResponse<ProjectListResponse>> getProjectListByMember(
      @PathVariable String employeeId, @RequestParam int page, @RequestParam int size) {
    return ApiResponse.success(
        projectQueryService.findProjectListByMemberId(employeeId, page, size));
  }
}
