package com.nexus.sion.feature.member.command.application.controller;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateRequest;
import com.nexus.sion.feature.member.command.application.service.CertificateCommandService;
import com.nexus.sion.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateCommandController {

    private final CertificateCommandService certificateService;

    /* 관리자의 자격증 종류 등록 */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerCertificate(
            @RequestBody @Valid CertificateRequest request) {
        certificateService.registerCertificate(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
