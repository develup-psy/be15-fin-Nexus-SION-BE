package com.nexus.sion.feature.member.command.application.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.dto.request.MemberAddRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberStatusUpdateRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberUpdateRequest;
import com.nexus.sion.feature.member.command.application.service.MemberCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "", description = "")
public class MemberCommandController {

  private final MemberCommandService memberCommandService;

  @Operation(summary = "회원 가입", description = "회원 가입 기능")
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<Void>> register(@RequestBody MemberCreateRequest request) {
    memberCommandService.registerUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  @Operation(summary = "구성원 등록", description = "구성원 등록 기능")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> registerMembers(
      @RequestBody @Valid List<MemberAddRequest> requests) {

    memberCommandService.addMembers(requests);

    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "구성원 수정", description = "구성원 수정 기능")
  @PutMapping("/{employeeId}")
  public ResponseEntity<ApiResponse<Void>> updateMember(
      @PathVariable String employeeId, @RequestBody @Valid MemberUpdateRequest request) {
    memberCommandService.updateMember(employeeId, request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "구성원 삭제", description = "구성원 삭제 기능")
  @DeleteMapping("/{employeeId}")
  public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable String employeeId) {
    memberCommandService.deleteMember(employeeId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PatchMapping("/{employeeId}/status")
  public ResponseEntity<ApiResponse<Void>> updateMemberStatus(
      @PathVariable String employeeId, @RequestBody MemberStatusUpdateRequest request) {
    memberCommandService.updateMemberStatus(employeeId, request.status());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
