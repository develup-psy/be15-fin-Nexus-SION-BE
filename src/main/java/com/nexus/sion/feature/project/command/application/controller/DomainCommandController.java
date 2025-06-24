package com.nexus.sion.feature.project.command.application.controller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.command.application.dto.request.DomainRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/domains")
public class DomainCommandController {

    @PostMapping
    @Operation(summary = "도메인 등록", description = "새로운 도메인을 시스템에 등록합니다.")
    public ResponseEntity<ApiResponse<Void>> registerDomain(
            @RequestBody DomainRequest request) {

    }
}
