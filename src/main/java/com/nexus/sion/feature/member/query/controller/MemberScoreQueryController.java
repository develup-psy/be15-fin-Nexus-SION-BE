package com.nexus.sion.feature.member.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberScoreHistoryResponse;
import com.nexus.sion.feature.member.query.service.MemberScoreHistoryQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/member-scores")
@RequiredArgsConstructor
public class MemberScoreQueryController {

  private final MemberScoreHistoryQueryService scoreHistoryQueryService;

  @GetMapping("/{employeeId}")
  public ResponseEntity<ApiResponse<MemberScoreHistoryResponse>> getScoreHistory(
      @PathVariable String employeeId) {
    MemberScoreHistoryResponse response = scoreHistoryQueryService.getScoreHistory(employeeId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
