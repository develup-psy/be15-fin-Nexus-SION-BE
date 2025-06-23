package com.nexus.sion.feature.project.command.application.controller;

import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.application.service.ProjectCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectCommandController {

    private final ProjectCommandService projectCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectRegisterResponse>> registerProject(
            @RequestBody ProjectRegisterRequest request) {
        ProjectRegisterResponse response = projectCommandService.registerProject(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
