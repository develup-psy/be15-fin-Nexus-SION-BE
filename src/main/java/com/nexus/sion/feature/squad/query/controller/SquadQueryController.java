package com.nexus.sion.feature.squad.query.controller;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadCandidateResponse;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/squads")
@RequiredArgsConstructor
public class SquadQueryController {

  private final SquadQueryService squadQueryService;

  /* 스쿼드 목록 조회 */
  @GetMapping("/project/{projectCode}")
  public ResponseEntity<ApiResponse<PageResponse<SquadListResponse>>> getSquads(
      @PathVariable String projectCode, @ModelAttribute SquadListRequest request) {
    request.setProjectCode(projectCode);
    PageResponse<SquadListResponse> response = squadQueryService.getSquads(request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /* 스쿼드 상세 조회 */
  @GetMapping("/{squadCode}")
  public ResponseEntity<ApiResponse<SquadDetailResponse>> getSquadDetail(@PathVariable String squadCode) {
    SquadDetailResponse response = squadQueryService.getSquadDetailByCode(squadCode);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /* 스쿼드 직무별 추천 조회 */
  @GetMapping("/candidates")
  public ResponseEntity<ApiResponse<SquadCandidateResponse>> getCandidates(
      @RequestParam String projectId) {

    return ResponseEntity.ok(
        ApiResponse.success(squadQueryService.findCandidatesByRoles(projectId)));
  }
}
