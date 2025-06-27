package com.nexus.sion.feature.project.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ClientCompanySearchRequest;
import com.nexus.sion.feature.project.query.dto.response.ClientCompanyDto;
import com.nexus.sion.feature.project.query.service.ClientCompanyQueryService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/client-companies")
public class ClientCompanyQueryController {

  private final ClientCompanyQueryService clientCompanyQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<ClientCompanyDto>>> getAllClientCompanies(
      @Validated @ModelAttribute ClientCompanySearchRequest request) {
    PageResponse<ClientCompanyDto> clientCompanies =
        clientCompanyQueryService.findClientCompanies(request);
    return ResponseEntity.ok(ApiResponse.success(clientCompanies));
  }
}
