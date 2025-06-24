package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.dto.response.PositionResponse;
import com.nexus.sion.feature.member.query.service.PositionQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionQueryController {

  private final PositionQueryService positionQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<PositionResponse>>> getAllPositions() {
    List<PositionResponse> positions = positionQueryService.getPositions();
    return ResponseEntity.ok(ApiResponse.success(positions));
  }
}
