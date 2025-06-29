package com.nexus.sion.feature.squad.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResultResponse;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/squads")
@RequiredArgsConstructor
public class SquadQueryController {

  private final SquadQueryService squadQueryService;

  @GetMapping("/project/{projectCode}")
  public ResponseEntity<SquadListResultResponse> getSquads(
      @PathVariable String projectCode, @ModelAttribute SquadListRequest request) {
    request.setProjectCode(projectCode);
    return ResponseEntity.ok(squadQueryService.findSquads(request));
  }

  @GetMapping("/{squadCode}")
  public ResponseEntity<SquadDetailResponse> getSquadDetail(@PathVariable String squadCode) {
    SquadDetailResponse response = squadQueryService.getSquadDetailByCode(squadCode);
    return ResponseEntity.ok(response);
  }
}
