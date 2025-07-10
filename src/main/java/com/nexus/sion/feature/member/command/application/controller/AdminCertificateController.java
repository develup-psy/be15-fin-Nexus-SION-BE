package com.nexus.sion.feature.member.command.application.controller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.command.application.service.UserCertificateHistoryService;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/certificates")
@RequiredArgsConstructor
public class AdminCertificateController {

    private final UserCertificateHistoryService userCertificateHistoryService;

    // 1. 승인 대기 자격증 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserCertificateHistoryResponse>>> getAllCertificates() {
        List<UserCertificateHistoryResponse> result = userCertificateHistoryService.getAllCertificates();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 2. 자격증 승인
    @PatchMapping("/{certificateRequestId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveCertificate(@PathVariable Long certificateRequestId) {
        userCertificateHistoryService.approveUserCertificate(certificateRequestId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 3. 자격증 반려
    @PatchMapping("/{certificateRequestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectCertificate(
            @PathVariable Long certificateRequestId,
            @RequestBody CertificateRejectRequest rejectedReason
    ) {
        userCertificateHistoryService.rejectUserCertificate(certificateRequestId, rejectedReason);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
