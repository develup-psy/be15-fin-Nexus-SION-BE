package com.nexus.sion.feature.member.command.application.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateCreateRequest;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateUpdateRequest;
import com.nexus.sion.feature.member.command.application.service.AdminCertificateCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateCommandController {

  private final AdminCertificateCommandService certificateService;

  /* 관리자의 자격증 종류 등록 */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Void>> registerCertificate(
      @RequestBody @Valid CertificateCreateRequest request) {
    certificateService.registerCertificate(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  /* 관리자의 자격증 종류 수정 */
  @PatchMapping("/{certificateName}")
  public ResponseEntity<ApiResponse<Void>> updateCertificate(
      @PathVariable String certificateName, @Valid @RequestBody CertificateUpdateRequest request) {
    certificateService.updateCertificate(certificateName, request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  /* 관리자의 자격증 종류 삭제 */
  @DeleteMapping("/{certificateName}")
  public ResponseEntity<ApiResponse<Void>> deleteCertificate(@PathVariable String certificateName) {
    certificateService.deleteCertificate(certificateName);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
