package com.nexus.sion.feature.squad.command.application.controller;

import com.nexus.sion.feature.squad.command.application.service.SquadLifeCycle;
import com.nexus.sion.feature.squad.command.application.service.SquadManualService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.application.dto.response.SquadRecommendationResponse;
import com.nexus.sion.feature.squad.command.application.service.SquadRecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/squads")
@RequiredArgsConstructor
public class SquadCommandController {

  private final SquadRecommendationService squadCommandService;
  private final SquadManualService squadManualService;
  private final SquadLifeCycle squadLifeCycle;

  @PostMapping("/manual")
  public ResponseEntity<ApiResponse<Void>> registerManualSquad(
      @RequestBody @Valid SquadRegisterRequest request) {
    squadManualService.registerManualSquad(request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/manual")
  public ResponseEntity<ApiResponse<Void>> updateManualSquad(
      @RequestBody @Valid SquadUpdateRequest request) {
    squadManualService.updateManualSquad(request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/{squadCode}")
  public ResponseEntity<Void> deleteSquad(@PathVariable String squadCode) {
    squadManualService.deleteSquad(squadCode);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/recommendation")
  public ResponseEntity<ApiResponse<SquadRecommendationResponse>> recommendSquad(
      @RequestBody @Valid SquadRecommendationRequest request) {

    SquadRecommendationResponse result = squadCommandService.recommendSquad(request);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @PatchMapping("/{squadCode}/confirm")
  public ResponseEntity<ApiResponse<Void>> confirmSquad(@PathVariable String squadCode) {
    squadLifeCycle.confirmSquad(squadCode);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
