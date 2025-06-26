package com.nexus.sion.feature.project.command.application.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyUpdateRequest;
import com.nexus.sion.feature.project.command.application.service.ClientCompanyCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/client-companies")
@Tag(name = "", description = "")
public class ClientCompanyCommandController {

  private final ClientCompanyCommandService clientCompanyCommandService;

  @Operation(summary = "고객사 등록", description = "고객사 등록 기능")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> register(
      @RequestBody @Valid ClientCompanyCreateRequest request) {
    clientCompanyCommandService.registerClientCompany(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  @Operation(summary = "고객사 수정", description = "고객사 수정 기능")
  @PatchMapping("/{clientCode}")
  public ResponseEntity<ApiResponse<Void>> update(
      @RequestBody @Valid ClientCompanyUpdateRequest request, @PathVariable String clientCode) {
    clientCompanyCommandService.updateClientCompany(request, clientCode);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "고객사 삭제", description = "고객사 삭제 기능")
  @DeleteMapping("/{clientCode}")
  public ResponseEntity<ApiResponse<Void>> delete(
          @PathVariable String clientCode) {
    clientCompanyCommandService.deleteClientCompany(clientCode);
    return ResponseEntity.noContent().build();
  }
}
