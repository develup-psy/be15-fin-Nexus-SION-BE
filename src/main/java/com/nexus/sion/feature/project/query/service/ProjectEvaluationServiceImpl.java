package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import com.nexus.sion.feature.project.command.domain.repository.ProjectFpSummaryRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.query.dto.internal.ProjectEvaluationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ProjectEvaluationServiceImpl implements ProjectEvaluationService{

    private final ProjectRepository projectRepository;
    private final ProjectFpSummaryRepository projectFpSummaryRepository;

    public ProjectEvaluationInfo getEvaluationInfo(String projectId) {
        Project project = projectRepository.findByProjectCode(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        BigDecimal maxBudget = project.getBudget();

        Integer maxDuration = null;
        if (project.getExpectedEndDate() != null && project.getStartDate() != null) {
            long days = ChronoUnit.DAYS.between(project.getStartDate(), project.getExpectedEndDate());
            maxDuration = (int) Math.ceil(days / 30.0);
        }

        int totalFP = projectFpSummaryRepository.findByProjectCode(projectId)
                .map(ProjectFpSummary::getTotalFp)
                .orElseThrow(() -> new BusinessException(ErrorCode.FP_NOT_FOUND));

        return new ProjectEvaluationInfo(maxBudget, maxDuration, totalFP);
    }
}
