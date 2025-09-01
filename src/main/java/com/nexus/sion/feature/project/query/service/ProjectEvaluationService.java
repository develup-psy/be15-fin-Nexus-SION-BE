package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.feature.project.query.dto.internal.ProjectEvaluationInfo;

public interface ProjectEvaluationService {
    ProjectEvaluationInfo getEvaluationInfo(String projectId);
}
