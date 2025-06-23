package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberTechStackResponse;
import com.nexus.sion.feature.member.query.service.MemberTechStackQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberTechStackQueryController {

  private final MemberTechStackQueryService memberTechStackQueryService;

  @GetMapping("/{employeeId}/tech-stacks")
  public ResponseEntity<ApiResponse<List<MemberTechStackResponse>>> getTechStacksByDeveloper(
      @PathVariable String employeeId) {
    List<MemberTechStackResponse> result = memberTechStackQueryService.getTechStacks(employeeId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
