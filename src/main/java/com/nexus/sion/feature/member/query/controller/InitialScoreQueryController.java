package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.InitialScoreResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.service.InitialScoreQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/initial-scores")
@Tag(name = "연차별 개발자 초기 스택 점수 조회", description = "초기 설정값을 조회합니다.")
public class InitialScoreQueryController {

  private final InitialScoreQueryService initialScoreQueryService;

  @Operation(summary = "연차별 초기 스택 점수 조회", description = "연차별 초기 스택 점수 조회 가능")
  @GetMapping
  public ResponseEntity<ApiResponse<List<InitialScoreResponseDto>>> getInitialScores() {
    return ResponseEntity.ok(ApiResponse.success(initialScoreQueryService.getInitialScores()));
  }
}
