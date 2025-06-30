package com.nexus.sion.feature.evaluation.command.application.service;

import com.nexus.sion.feature.evaluation.command.application.dto.request.ProjectEvaluationRequest;

public interface ProjectEvaluationCommandService {
  void evaluateProject(ProjectEvaluationRequest request);
}
