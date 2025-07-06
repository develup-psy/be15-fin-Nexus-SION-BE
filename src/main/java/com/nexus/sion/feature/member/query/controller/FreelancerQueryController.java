package com.nexus.sion.feature.member.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.response.FreelancerListResponse;
import com.nexus.sion.feature.member.query.service.FreelancerQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/freelancers")
public class FreelancerQueryController {

  private final FreelancerQueryService freelancerQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<FreelancerListResponse>>> getFreelancerList(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
    PageResponse<FreelancerListResponse> result = freelancerQueryService.getFreelancers(page, size);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
