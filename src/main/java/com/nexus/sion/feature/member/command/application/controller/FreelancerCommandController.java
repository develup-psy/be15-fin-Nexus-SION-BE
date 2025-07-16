package com.nexus.sion.feature.member.command.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.service.FreelancerCommandServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/freelancers")
public class FreelancerCommandController {

  private final FreelancerCommandServiceImpl freelancerCommandService;

  @PostMapping("/{freelancerId}/register")
  public ResponseEntity<ApiResponse<Void>> registerAsMember(@PathVariable String freelancerId) {
    freelancerCommandService.registerFreelancerAsMember(freelancerId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
