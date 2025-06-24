package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.dto.response.DepartmentResponse;
import com.nexus.sion.feature.member.query.service.DepartmentQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentQueryController {

  private final DepartmentQueryService departmentQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
    List<DepartmentResponse> departments = departmentQueryService.getDepartments();
    return ResponseEntity.ok(ApiResponse.success(departments));
  }
}
