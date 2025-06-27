package com.nexus.sion.feature.member.query.controller;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.request.MemberSquadSearchRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberDetailResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberSquadListResponse;
import com.nexus.sion.feature.member.query.service.MemberQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/members")
@Slf4j
public class MemberQueryController {

  private final MemberQueryService memberQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<MemberListResponse>>> getMembers(
      @ModelAttribute MemberListRequest request) {
    PageResponse<MemberListResponse> pageResponse = memberQueryService.getAllMembers(request);
    return ResponseEntity.ok(ApiResponse.success(pageResponse));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<PageResponse<MemberListResponse>>> searchDevelopers(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(memberQueryService.searchMembers(keyword, page, size)));
  }

  @GetMapping("/{employeeId}")
  public ResponseEntity<ApiResponse<MemberDetailResponse>> getDeveloperDetail(
      @PathVariable String employeeId) {
    MemberDetailResponse response = memberQueryService.getMemberDetail(employeeId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/squad-search")
  public ResponseEntity<ApiResponse<PageResponse<MemberSquadListResponse>>> squadSearchDevelopers(
      @ModelAttribute MemberSquadSearchRequest request) {
    MemberListQuery query = request.toQuery();

    return ResponseEntity.ok(ApiResponse.success(memberQueryService.squadSearchMembers(query)));
  }



}
