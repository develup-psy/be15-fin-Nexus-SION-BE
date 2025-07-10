package com.nexus.sion.feature.member.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.response.FreelancerDetailResponse;
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
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    PageResponse<FreelancerListResponse> result = freelancerQueryService.getFreelancers(page, size);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/{freelancerId}")
  public ResponseEntity<ApiResponse<FreelancerDetailResponse>> getFreelancerDetail(
      @PathVariable String freelancerId) {
    FreelancerDetailResponse detail = freelancerQueryService.getFreelancerDetail(freelancerId);
    return ResponseEntity.ok(ApiResponse.success(detail));
  }
}
