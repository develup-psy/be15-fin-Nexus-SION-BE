package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;
import com.nexus.sion.feature.member.query.service.UserCertificateHistoryQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-certificates")
public class UserCertificateHistoryQueryController {

  private final UserCertificateHistoryQueryService userCertificateHistoryQueryService;

  @PreAuthorize("hasAuthority('ADMIN') or #employeeId == authentication.principal.username")
  @GetMapping("/{employeeId}")
  public ResponseEntity<ApiResponse<List<UserCertificateHistoryResponse>>>
      getCertificatesByEmployeeId(@PathVariable String employeeId) {
    List<UserCertificateHistoryResponse> result =
        userCertificateHistoryQueryService.getMyCertificates(employeeId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
