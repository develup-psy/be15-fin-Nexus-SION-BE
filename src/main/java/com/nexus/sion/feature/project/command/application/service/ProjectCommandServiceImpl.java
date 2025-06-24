package com.nexus.sion.feature.project.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.domain.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectCommandServiceImpl implements ProjectCommandService {

  private final ProjectCommandRepository projectCommandRepository;
  private final ProjectAndJobRepository projectAndJobRepository;
  private final JobAndTechStackRepository jobAndTechStackRepository;

  @Override
  public ProjectRegisterResponse registerProject(ProjectRegisterRequest request) {
    if (projectCommandRepository.existsByProjectCode(request.getProjectCode())) {
      throw new BusinessException(ErrorCode.PROJECT_CODE_DUPLICATED);
    }

    Project project =
        Project.builder()
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

    saveJobsAndTechStacks(request);
    return new ProjectRegisterResponse(request.getProjectCode());
  }

  @Override
  public void updateProject(ProjectRegisterRequest request) {
    Project project =
        projectCommandRepository
            .findById(request.getProjectCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    project.setName(request.getName());
    project.setDescription(request.getDescription());
    project.setTitle(request.getTitle());
    project.setBudget(request.getBudget());
    project.setStartDate(request.getStartDate());
    project.setExpectedEndDate(request.getExpectedEndDate());
    project.setNumberOfMembers(request.getNumberOfMembers());
    project.setClientCode(request.getClientCode());
    project.setRequestSpecificationUrl(request.getRequestSpecificationUrl());
    project.setUpdatedAt(LocalDateTime.now());
    projectCommandRepository.save(project);

    var projectAndJobs = projectAndJobRepository.findByProjectCode(request.getProjectCode());
    projectAndJobs.forEach(
        job -> {
          jobAndTechStackRepository.deleteByProjectJobId(job.getId());
        });
    projectAndJobRepository.deleteByProjectCode(request.getProjectCode());

    saveJobsAndTechStacks(request);
  }

  private void saveJobsAndTechStacks(ProjectRegisterRequest request) {
    request
        .getJobs()
        .forEach(
            job -> {
              ProjectAndJob projectAndJob =
                  ProjectAndJob.builder()
                      .projectCode(request.getProjectCode())
                      .jobName(job.getJobName())
                      .requiredNumber(job.getRequiredNumber())
                      .createdAt(LocalDateTime.now())
                      .updatedAt(LocalDateTime.now())
                      .build();
              projectAndJobRepository.save(projectAndJob);

              job.getTechStacks()
                  .forEach(
                      tech -> {
                        JobAndTechStack jobAndTechStack =
                            JobAndTechStack.builder()
                                .projectJobId(projectAndJob.getId())
                                .techStackName(tech.getTechStackName())
                                .priority(tech.getPriority())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        jobAndTechStackRepository.save(jobAndTechStack);
                      });
            });
  }
}
