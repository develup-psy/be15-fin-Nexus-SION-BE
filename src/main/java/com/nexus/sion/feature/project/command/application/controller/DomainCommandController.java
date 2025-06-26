package com.nexus.sion.feature.project.command.application.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.command.application.dto.request.DomainRequest;
import com.nexus.sion.feature.project.command.application.service.DomainCommandService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/domains")
public class DomainCommandController {

  private final DomainCommandService domainCommandService;

  @PostMapping
  @Operation(summary = "도메인 등록", description = "새로운 도메인을 시스템에 등록합니다.")
  public ResponseEntity<ApiResponse<Void>> registerDomain(
      @RequestBody @Valid DomainRequest request) {
    if (domainCommandService.registerDomain(request)) {
      return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/{domainName}")
  @Operation(summary = "도메인 삭제", description = "도메인을 시스템에서 삭제합니다.")
  public ResponseEntity<ApiResponse<Void>> removeDomain(@PathVariable String domainName) {
    domainCommandService.removeDomain(domainName);
    return ResponseEntity.noContent().build();
  }
}
