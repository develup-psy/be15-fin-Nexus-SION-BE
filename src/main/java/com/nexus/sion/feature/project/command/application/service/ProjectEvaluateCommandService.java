package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.FunctionScoreDTO;

import java.util.List;

public interface ProjectEvaluateCommandService {
    void evaluateFunctionScores(FunctionScoreDTO requests);
}
