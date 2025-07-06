package com.nexus.sion.feature.project.command.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryRequestDto;
import com.nexus.sion.feature.project.command.application.service.DeveloperProjectWorkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/dev-project-works")
@RequiredArgsConstructor
public class DeveloperProjectWorkController {

  private final DeveloperProjectWorkService developerProjectWorkService;

  @PostMapping
  public ResponseEntity<ApiResponse<Long>> createWorkHistory(
      @RequestBody WorkHistoryRequestDto dto) {
    Long id = developerProjectWorkService.requestWork(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(id));
  }

  @PutMapping("/{id}/approve")
  public ResponseEntity<ApiResponse<Void>> approveWorkHistory(
      @PathVariable Long id, @RequestParam String adminId) {
    developerProjectWorkService.approve(id, adminId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/{id}/reject")
  public ResponseEntity<ApiResponse<Void>> rejectWorkHistory(
      @PathVariable Long id, @RequestParam String adminId) {
    developerProjectWorkService.reject(id, adminId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
