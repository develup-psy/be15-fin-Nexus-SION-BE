package com.nexus.sion.feature.project.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.query.dto.response.JobListResponse;
import com.nexus.sion.feature.project.query.service.JobQueryService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/jobs")
public class JobQueryController {

  private final JobQueryService jobQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<JobListResponse>> getAllDomains() {
    return ResponseEntity.ok(
        ApiResponse.success(new JobListResponse(jobQueryService.findAllJobs())));
  }
}
