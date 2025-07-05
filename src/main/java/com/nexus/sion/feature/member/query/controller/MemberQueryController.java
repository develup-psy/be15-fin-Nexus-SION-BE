package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.jooq.generated.enums.GradeGradeCode;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
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

  @PostMapping("/squad-search")
  public ResponseEntity<ApiResponse<PageResponse<MemberSquadListResponse>>> squadSearchDevelopers(
      @RequestBody MemberSquadSearchRequest request) {

    // 1. Status 파싱
    MemberStatus parsedStatus = null;
    if (request.getStatus() != null && !request.getStatus().isBlank()) {
      try {
        parsedStatus = MemberStatus.valueOf(request.getStatus().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
      }
    }

    // 2. Grade 파싱
    List<GradeGradeCode> parsedGrades = null;
    if (request.getGrades() != null && !request.getGrades().isEmpty()) {
      try {
        parsedGrades =
            request.getGrades().stream().map(s -> GradeGradeCode.valueOf(s.toUpperCase())).toList();
      } catch (IllegalArgumentException e) {
        throw new BusinessException(ErrorCode.INVALID_GRADE);
      }
    }

    // 3. Role 필터링 파싱 (INSIDER, OUTSIDER)
    List<String> memberRoles = request.getMemberRoles();

    // 4. MemberListQuery 생성
    MemberListQuery query =
        new MemberListQuery(
            request.getKeyword(),
            parsedStatus,
            parsedGrades,
            request.getStacks(),
            request.getSortBy(),
            request.getSortDir(),
            request.getPage(),
            request.getSize(),
            memberRoles);

    // 5. 서비스 호출 및 응답
    PageResponse<MemberSquadListResponse> result = memberQueryService.squadSearchMembers(query);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
