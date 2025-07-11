package com.nexus.sion.feature.member.command.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.service.FreelancerCommandServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/freelancers")
public class FreelancerCommandController {

  private final FreelancerCommandServiceImpl freelancerCommandService;

  @PostMapping("/{freelancerId}/register")
  public ResponseEntity<ApiResponse<Void>> registerAsMember(@PathVariable String freelancerId, @RequestParam("file") MultipartFile multipartFile) {
    freelancerCommandService.registerFreelancerAsMember(freelancerId, multipartFile);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
