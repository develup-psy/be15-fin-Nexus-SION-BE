package com.nexus.sion.feature.project.command.application.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.command.application.dto.request.JobRequest;
import com.nexus.sion.feature.project.command.application.service.JobCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobs")
@Tag(name = "JobCommand", description = "직무 관련 API")
public class JobCommandController {

  private final JobCommandService jobCommandService;

  @PostMapping
  @Operation(summary = "직무 등록", description = "새로운 직무를 시스템에 등록합니다.")
  public ResponseEntity<ApiResponse<Void>> registerJob(@RequestBody @Valid JobRequest request) {
    if (jobCommandService.registerJob(request)) {
      return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
