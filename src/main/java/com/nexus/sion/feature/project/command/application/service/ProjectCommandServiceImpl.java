package com.nexus.sion.feature.project.command.application.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

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
            .domainName(request.getDomainName())
            .description(request.getDescription())
            .title(request.getTitle())
            .budget(request.getBudget())
            .startDate(request.getStartDate())
            .expectedEndDate(request.getExpectedEndDate())
            .status(Project.ProjectStatus.WAITING)
            .numberOfMembers(request.getNumberOfMembers())
            .clientCode(request.getClientCode())
            .requestSpecificationUrl(request.getRequestSpecificationUrl())
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

    project.setDomainName(request.getDomainName());
    project.setDescription(request.getDescription());
    project.setTitle(request.getTitle());
    project.setBudget(request.getBudget());
    project.setStartDate(request.getStartDate());
    project.setExpectedEndDate(request.getExpectedEndDate());
    project.setNumberOfMembers(request.getNumberOfMembers());
    project.setClientCode(request.getClientCode());
    project.setRequestSpecificationUrl(request.getRequestSpecificationUrl());
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
                                .build();
                        jobAndTechStackRepository.save(jobAndTechStack);
                      });
            });
  }

  @Override
  public void deleteProject(String projectCode) {
    Project project =
        projectCommandRepository
            .findById(projectCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    var projectAndJobs = projectAndJobRepository.findByProjectCode(projectCode);
    projectAndJobs.forEach(
        job -> {
          jobAndTechStackRepository.deleteByProjectJobId(job.getId());
        });
    projectAndJobRepository.deleteByProjectCode(projectCode);

    projectCommandRepository.delete(project);
  }

  @Override
  public void updateProjectStatus(String projectCode, Project.ProjectStatus status) {
    Project project =
        projectCommandRepository
            .findById(projectCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    project.setStatus(status);
    if (status == Project.ProjectStatus.COMPLETE) {
      project.setActualEndDate(LocalDate.now());
    } else {
      project.setActualEndDate(null);
    }
    projectCommandRepository.save(project);
  }

  @Override
  public Map<String, Long> findProjectAndJobIdMap(String projectId) {
    return projectAndJobRepository.findByProjectCode(projectId).stream()
            .collect(Collectors.toMap(
                    ProjectAndJob::getJobName,
                    ProjectAndJob::getId
            ));
  }
}
