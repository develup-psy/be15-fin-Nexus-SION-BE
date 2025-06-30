package com.nexus.sion.feature.project.command.application.dto.response;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFunctionEstimate;

import java.util.List;

public record ProjectAnalysisResult(
        ProjectFpSummary summary,
        List<ProjectFunctionEstimate> functions
) {}
