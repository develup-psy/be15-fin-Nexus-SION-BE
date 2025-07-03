package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.dto.response.GradeDto;
import com.nexus.sion.feature.member.query.service.GradeQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/grades")
@Tag(name = "등급 조회", description = "등급별 단가 및 생산성 조회 API")
public class GradeQueryController {

  private final GradeQueryService gradeQueryService;

  @Operation(summary = "구간별 단가 조회", description = "구간별 단가 조회")
  @GetMapping
  public ResponseEntity<ApiResponse<List<GradeDto>>> getUnitPriceByGrade() {
    return ResponseEntity.ok(ApiResponse.success(gradeQueryService.getGrade()));
  }
}
