package com.nexus.sion.feature.member.command.application.controller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.command.application.service.FreelancerCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/freelancers")
public class FreelancerCommandController {

    private final FreelancerCommandService freelancerCommandService;

    @PostMapping("/{freelancerId}/register")
    public ResponseEntity<ApiResponse<Void>> registerAsMember(@PathVariable String freelancerId) {
        freelancerCommandService.registerFreelancerAsMember(freelancerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}