package com.nexus.sion.feature.member.command.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.dto.request.UnitPriceSetRequest;
import com.nexus.sion.feature.member.command.application.service.GradeCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/grades")
@Tag(name = "등급 관리", description = "등급별 단가 및 생산성 관리 API")
public class GradeCommandController {

  private final GradeCommandService gradeCommandService;

  @Operation(summary = "구간별 단가 설정", description = "구간별 단가 설정 기능")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> setUnitPriceByGrade(
      @RequestBody UnitPriceSetRequest request) {
    gradeCommandService.setGrades(request);
    return ResponseEntity.ok(ApiResponse.success(null)); // 200 OK
  }
}
