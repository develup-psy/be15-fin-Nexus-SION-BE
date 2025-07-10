package com.nexus.sion.feature.project.command.application.controller;

import com.nexus.sion.feature.project.command.application.dto.FunctionScoreDTO;
import com.nexus.sion.feature.project.command.application.service.ProjectEvaluateCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/project-evaluate")
public class ProjectEvaluationController {
    private final ProjectEvaluateCommandService projectEvaluateCommandService;

    @PostMapping("/function-score")
    public ResponseEntity<Void> evaluateFunctionScores(@RequestBody FunctionScoreDTO dto) {
        projectEvaluateCommandService.evaluateFunctionScores(dto);
        return ResponseEntity.ok().build();
    }
}
