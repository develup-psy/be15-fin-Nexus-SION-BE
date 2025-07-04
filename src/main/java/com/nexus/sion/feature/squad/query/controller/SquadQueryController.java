package com.nexus.sion.feature.squad.query.controller;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadCandidateResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/squads")
@RequiredArgsConstructor
public class SquadQueryController {

  private final SquadQueryService squadQueryService;

  @GetMapping("/project/{projectCode}")
  public ResponseEntity<ApiResponse<SquadResponse>> getSquadsOrConfirmed(
      @PathVariable String projectCode, @ModelAttribute SquadListRequest request) {
    request.setProjectCode(projectCode);
    SquadResponse response = squadQueryService.findSquadsOrConfirmed(request);
      return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/{squadCode}")
  public ResponseEntity<SquadDetailResponse> getSquadDetail(@PathVariable String squadCode) {
    SquadDetailResponse response = squadQueryService.getSquadDetailByCode(squadCode);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/candidates")
  public ResponseEntity<ApiResponse<SquadCandidateResponse>> getCandidates(
      @RequestParam String projectId) {

    return ResponseEntity.ok(
        ApiResponse.success(squadQueryService.findCandidatesByRoles(projectId)));
  }
}
