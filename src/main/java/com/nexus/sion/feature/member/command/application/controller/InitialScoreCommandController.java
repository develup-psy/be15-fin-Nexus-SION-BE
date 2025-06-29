package com.nexus.sion.feature.member.command.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreSetRequest;
import com.nexus.sion.feature.member.command.application.service.InitialScoreCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/initial-scores")
@Tag(name = "연차별 개발자 초기 스택 점수 설정", description = "개발자 등록 시 초기 설정값을 관리합니다.")
public class InitialScoreCommandController {

  private final InitialScoreCommandService initialScoreCommandService;

  @Operation(summary = "연차별 초기 스택 점수 설정", description = "연차별 초기 스택 점수 설정 가능")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> setInitialScores(
      @RequestBody InitialScoreSetRequest request) {
    initialScoreCommandService.setInitialScores(request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
