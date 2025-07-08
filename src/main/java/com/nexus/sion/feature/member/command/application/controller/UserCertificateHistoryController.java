package com.nexus.sion.feature.member.command.application.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.dto.request.UserCertificateHistoryRequest;
import com.nexus.sion.feature.member.command.application.service.UserCertificateHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members/{employeeId}/certificates")
@RequiredArgsConstructor
public class UserCertificateHistoryController {

  private final UserCertificateHistoryService userCertificateHistoryService;

  /* 개발자의 자격증 등록 */
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> registerUserCertificate(
          @PathVariable String employeeId,
          @RequestBody @Valid UserCertificateHistoryRequest request
  ) {
    userCertificateHistoryService.registerUserCertificate(employeeId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }
}
