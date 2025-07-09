package com.nexus.sion.feature.project.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.query.dto.response.FunctionTypeDto;
import com.nexus.sion.feature.project.query.dto.response.WorkInfoQueryDto;
import com.nexus.sion.feature.project.query.dto.response.WorkRequestQueryDto;
import com.nexus.sion.feature.project.query.service.DeveloperProjectWorkQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dev-project-works")
@RequiredArgsConstructor
public class DeveloperProjectWorkQueryController {

  private final DeveloperProjectWorkQueryService developerProjectWorkQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<WorkRequestQueryDto>>> getAllRequests() {
    List<WorkRequestQueryDto> result = developerProjectWorkQueryService.getAllRequests();
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<List<WorkRequestQueryDto>>> getMyRequests(
      @AuthenticationPrincipal User user) {
    String employeeId = user.getUsername();
    List<WorkRequestQueryDto> result =
        developerProjectWorkQueryService.getRequestsByEmployeeId(employeeId);
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
}
