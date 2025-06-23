package com.nexus.sion.feature.techstack.command.application.controller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackCreateRequest;
import com.nexus.sion.feature.techstack.command.application.service.TechStackCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
 import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;

 @RestController
 @RequiredArgsConstructor
 @RequestMapping("/api/v1/tech-stack")
 @Tag(name = "TechStackCommand", description = "기술 스택 관련 API")
 public class TechStackCommandController {

  private final TechStackCommandService techStackCommandService;

  @PostMapping
  @Operation(summary = "기술 스택 등록", description = "새로운 기술 스택을 시스템에 등록합니다.")
  public ResponseEntity<ApiResponse<Void>> registerTechStack(@RequestBody TechStackCreateRequest request) {
   techStackCommandService.registerTechStack(request);
   return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

 }
