package com.nexus.sion.feature.project.query.controller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.query.dto.response.ClientCompanyListResponse;
import com.nexus.sion.feature.project.query.service.ClientCompanyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/client-companies")
public class ClientCompanyQueryController {

    private final ClientCompanyQueryService clientCompanyQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<ClientCompanyListResponse>> getAllClientCompanies() {
        return ResponseEntity.ok(
                ApiResponse.success(clientCompanyQueryService.findAllClientCompany())
        );
    }

}
