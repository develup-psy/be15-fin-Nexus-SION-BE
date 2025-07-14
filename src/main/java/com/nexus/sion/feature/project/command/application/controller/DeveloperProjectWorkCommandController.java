package com.nexus.sion.feature.project.command.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryAddRequestDto;
import com.nexus.sion.feature.project.command.application.service.DeveloperProjectWorkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/dev-project-works")
@RequiredArgsConstructor
public class DeveloperProjectWorkCommandController {

  private final DeveloperProjectWorkService developerProjectWorkService;

  @PutMapping("/{workId}/histories")
  public ResponseEntity<ApiResponse<Void>> addHistories(
      @PathVariable Long workId, @RequestBody WorkHistoryAddRequestDto requestDto) {
    developerProjectWorkService.addHistories(workId, requestDto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/{id}/approve")
  public ResponseEntity<ApiResponse<Void>> approveWorkHistory(
      @PathVariable Long id, @RequestParam String adminId) {
    developerProjectWorkService.approve(id, adminId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/{id}/reject")
  public ResponseEntity<ApiResponse<Void>> rejectWorkHistory(
      @PathVariable Long id, @RequestParam String adminId, @RequestParam String reason) {

    developerProjectWorkService.reject(id, adminId, reason);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
