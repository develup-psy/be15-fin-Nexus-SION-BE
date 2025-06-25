package com.nexus.sion.feature.project.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.query.dto.response.DomainListResponse;
import com.nexus.sion.feature.project.query.service.DomainQueryService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/domains")
public class DomainQueryController {

  private final DomainQueryService domainQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<DomainListResponse>> getAllDomains() {
    return ResponseEntity.ok(
        ApiResponse.success(new DomainListResponse(domainQueryService.findAllDomains())));
  }
}
