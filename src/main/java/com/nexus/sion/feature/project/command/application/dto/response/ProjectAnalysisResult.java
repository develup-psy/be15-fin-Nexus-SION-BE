package com.nexus.sion.feature.project.command.application.dto.response;

import java.util.List;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFunctionEstimate;

public record ProjectAnalysisResult(
    ProjectFpSummary summary, List<ProjectFunctionEstimate> functions) {}
