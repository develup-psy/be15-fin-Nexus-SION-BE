package com.nexus.sion.feature.project.query.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.MyProjectListRequest;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.request.ReplacementRecommendationRequest;
import com.nexus.sion.feature.project.query.dto.response.*;
import com.nexus.sion.feature.project.query.service.DeveloperProjectWorkQueryService;
import com.nexus.sion.feature.project.query.service.ProjectQueryService;
import com.nexus.sion.feature.project.query.service.ReplacementRecommendationService;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/projects")
@RequiredArgsConstructor
public class ProjectQueryController {

  private final ProjectQueryService projectQueryService;
  private final DeveloperProjectWorkQueryService developerProjectWorkQueryService;
  private final ReplacementRecommendationService replacementRecommendationService;

  // 목록 조회
  @PostMapping("/list")
  public ApiResponse<PageResponse<ProjectListResponse>> searchProjects(
      @RequestBody ProjectListRequest request) {
    PageResponse<ProjectListResponse> result = projectQueryService.findProjects(request);
    return ApiResponse.success(result);
  }

  // 내 프로젝트 목록 조회
  @PostMapping("/list/my")
  public ApiResponse<PageResponse<ProjectListResponse>> getMyProjects(
      @RequestBody MyProjectListRequest request) {
    PageResponse<ProjectListResponse> result =
        projectQueryService.getProjectsByEmployeeId(
            request.getEmployeeId(), request.getStatuses(), request.getPage(),
                request.getSize(), request.getSortBy(), request.getKeyword());
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

  // 프로젝트 이름, 코드 조회
  @GetMapping("/{id}/project-info")
  public ResponseEntity<ApiResponse<ProjectInfoDto>> getProjectInfo(@PathVariable Long id) {
    ProjectInfoDto response = developerProjectWorkQueryService.getProjectInfo(id);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // 프로젝트 스쿼드 인원 대체 추천
  @PostMapping("/replacement")
  public ResponseEntity<ApiResponse<List<DeveloperSummary>>> recommendReplacement(
      @RequestBody @Valid ReplacementRecommendationRequest request) {
    List<DeveloperSummary> candidates =
        replacementRecommendationService.recommendCandidates(
            request.getProjectCode(), request.getLeavingMember());
    return ResponseEntity.ok(ApiResponse.success(candidates));
  }
}
