package com.nexus.sion.feature.member.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;
import com.nexus.sion.feature.member.query.service.MemberQueryService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/members")
public class MemberQueryController {

  private final MemberQueryService memberQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<MemberListResponse>>> getMembers(
      @ModelAttribute MemberListRequest request) {
    PageResponse<MemberListResponse> pageResponse = memberQueryService.getAllMembers(request);
    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  @GetMapping("/search")
  public ApiResponse<PageResponse<MemberListResponse>> searchDevelopers(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.success(memberQueryService.searchMembers(keyword, page, size));
  }
}
