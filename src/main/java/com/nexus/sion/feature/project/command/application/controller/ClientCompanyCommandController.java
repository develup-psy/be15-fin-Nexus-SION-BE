package com.nexus.sion.feature.project.command.application.controller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.application.service.ClientCompanyCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/client-companies")
@Tag(name = "", description = "")
public class ClientCompanyCommandController {

    private final ClientCompanyCommandService clientCompanyCommandService;

    @Operation(summary = "고객사 등록", description = "고객사 등록 기능")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody ClientCompanyCreateRequest request) {
        clientCompanyCommandService.registerClientCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }
}
