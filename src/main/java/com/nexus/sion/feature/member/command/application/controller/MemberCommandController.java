package com.nexus.sion.feature.member.command.application.controller;

import com.nexus.sion.feature.member.command.application.dto.request.MemberAddRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.application.service.MemberCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "", description = "")
public class MemberCommandController {

  private final MemberCommandService userCommandService;

  @Operation(summary = "회원 가입", description = "회원 가입 기능")
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<Void>> register(@RequestBody MemberCreateRequest request) {
    userCommandService.registerUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
  }

  @Operation(summary = "구성원 등록", description = "구성원 등록 기능")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> registerMembers(
          @RequestBody @Valid List<MemberAddRequest> requests) {

    userCommandService.addMembers(requests);

    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
