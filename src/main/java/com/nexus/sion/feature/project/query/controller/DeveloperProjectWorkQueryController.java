package com.nexus.sion.feature.project.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.WorkRequestQueryDto;
import com.nexus.sion.feature.project.query.dto.response.DeveloperApprovalResponse;
import com.nexus.sion.feature.project.query.dto.response.FunctionTypeDto;
import com.nexus.sion.feature.project.query.dto.response.WorkInfoQueryDto;
import com.nexus.sion.feature.project.query.service.DeveloperProjectWorkQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dev-project-works")
@RequiredArgsConstructor
public class DeveloperProjectWorkQueryController {

  private final DeveloperProjectWorkQueryService developerProjectWorkQueryService;

  @GetMapping("/admin")
  public ResponseEntity<ApiResponse<PageResponse<WorkRequestQueryDto>>> getRequestsForAdmin(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    PageResponse<WorkRequestQueryDto> result =
        developerProjectWorkQueryService.getRequestsForAdmin(status, page, size);

    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<PageResponse<WorkRequestQueryDto>>> getMyRequests(
      @AuthenticationPrincipal User user,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    String employeeId = user.getUsername();
    PageResponse<WorkRequestQueryDto> result =
        developerProjectWorkQueryService.getRequestsByEmployeeId(employeeId, status, page, size);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/{projectWorkId}")
  public ResponseEntity<ApiResponse<WorkInfoQueryDto>> getProjectHistoryDetail(
      @PathVariable Long projectWorkId) {
    WorkInfoQueryDto result = developerProjectWorkQueryService.getRequestDetailById(projectWorkId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/function-types")
  public ApiResponse<List<FunctionTypeDto>> getFunctionTypes() {
    List<FunctionTypeDto> functionTypes = developerProjectWorkQueryService.getFunctionTypes();
    return ApiResponse.success(functionTypes);
  }

  @GetMapping("/{projectCode}/developer-approvals")
  public ResponseEntity<ApiResponse<List<DeveloperApprovalResponse>>> getDeveloperApprovals(
      @PathVariable String projectCode) {
    List<DeveloperApprovalResponse> approvals =
        developerProjectWorkQueryService.getDeveloperApprovals(projectCode);
    return ResponseEntity.ok(ApiResponse.success(approvals));
  }
}
