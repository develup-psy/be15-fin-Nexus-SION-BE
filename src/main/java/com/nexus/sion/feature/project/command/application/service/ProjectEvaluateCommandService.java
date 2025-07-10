package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.FunctionScoreDTO;

public interface ProjectEvaluateCommandService {
  void evaluateFunctionScores(FunctionScoreDTO requests);
}
