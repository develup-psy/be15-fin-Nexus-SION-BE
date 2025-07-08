package com.nexus.sion.feature.member.query.controller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.service.CertificateQueryService;
import com.nexus.sion.feature.member.query.dto.response.CertificateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/certificates")
public class CertificateQueryController {

  private final CertificateQueryService certificateQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CertificateResponse>>> getAllCertificates() {
    List<CertificateResponse> certificates = certificateQueryService.getAllCertificates();
    return ResponseEntity.ok(ApiResponse.success(certificates));
  }
}
