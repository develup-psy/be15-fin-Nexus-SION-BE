package com.nexus.sion.feature.project.command.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.repository.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectCommandServiceImpl implements ProjectCommandService {

    private final ProjectCommandRepository projectCommandRepository;
    private final ProjectAndJobRepository projectAndJobRepository;
    private final JobAndTechStackRepository jobAndTechStackRepository;

    @Override
    public ProjectRegisterResponse registerProject(ProjectRegisterRequest request) {
        Project project = Project.builder()
                .projectCode(request.getProjectCode())
                .name(request.getName())
                .description(request.getDescription())
                .title(request.getTitle())
                .budget(request.getBudget())
                .startDate(request.getStartDate())
                .expectedEndDate(request.getExpectedEndDate())
                .status(Project.ProjectStatus.WAITING)
                .numberOfMembers(request.getNumberOfMembers())
                .clientCode(request.getClientCode())
                .requestSpecificationUrl(request.getRequestSpecificationUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        projectCommandRepository.save(project);

        request.getJobs().forEach(job -> {
            ProjectAndJob projectAndJob = ProjectAndJob.builder()
                    .projectCode(request.getProjectCode())
                    .jobName(job.getJobName())
                    .requiredNumber(job.getRequiredNumber())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            projectAndJobRepository.save(projectAndJob);

            job.getTechStacks().forEach(tech -> {
                JobAndTechStack jobAndTechStack = JobAndTechStack.builder()
                        .projectAndJob(projectAndJob.getId())
                        .techStackId(tech.getTechStackId())
                        .priority(tech.getPriority())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                jobAndTechStackRepository.save(jobAndTechStack);
            });
        });

        return new ProjectRegisterResponse(request.getProjectCode());
    }
}
