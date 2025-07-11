package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<List<UserCertificateHistoryResponse>>> getMyCertificates(
      @AuthenticationPrincipal Long memberId) {
    List<UserCertificateHistoryResponse> result =
        userCertificateHistoryQueryService.getMyCertificates(memberId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
