package com.nexus.sion.feature.project.command.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.domain.repository.*;
import com.nexus.sion.feature.project.command.domain.service.ProjectAnalysisService;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkRepository;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectCommandServiceImpl implements ProjectCommandService {

  private final ProjectCommandRepository projectCommandRepository;
  private final ProjectAndJobRepository projectAndJobRepository;
  private final JobAndTechStackRepository jobAndTechStackRepository;
  private final ProjectAnalysisService projectAnalysisService;
  private final ProjectRepository projectRepository;
  private final DeveloperProjectWorkRepository developerProjectWorkRepository;
  private final SquadEmployeeCommandRepository squadEmployeeCommandRepository;

  @Override
  public ProjectRegisterResponse registerProject(ProjectRegisterRequest request) {
    String newProjectCode = generateNextProjectCode(request.getClientCode());

    Project project =
        Project.builder()
            .projectCode(newProjectCode)
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

    ProjectRegisterRequest copyRequest =
        ProjectRegisterRequest.copyWithProjectCode(request, newProjectCode);
    saveJobsAndTechStacks(copyRequest);

    return new ProjectRegisterResponse(newProjectCode);
  }

  private String generateNextProjectCode(String clientCode) {
    List<String> codes = projectRepository.findProjectCodesByClientCode(clientCode);

    int maxNumber = 0;
    for (String code : codes) {
      try {
        String suffix = code.substring(clientCode.length() + 1); // "na001_3" -> "3"
        int number = Integer.parseInt(suffix);
        if (number > maxNumber) maxNumber = number;
      } catch (Exception e) {
        log.warn("프로젝트 코드 파싱 실패: {}", code);
      }
    }

    return clientCode + "_" + (maxNumber + 1);
  }

  @Override
  @Transactional
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
    projectAndJobs.forEach(job -> jobAndTechStackRepository.deleteByProjectJobId(job.getId()));
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
    projectAndJobs.forEach(job -> jobAndTechStackRepository.deleteByProjectJobId(job.getId()));
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
      createDeveloperWorkRecords(projectCode);
    } else {
      project.setActualEndDate(null);
    }
    projectCommandRepository.save(project);
  }

  @Override
  public Map<String, Long> findProjectAndJobIdMap(String projectId) {
    return projectAndJobRepository.findByProjectCode(projectId).stream()
        .collect(Collectors.toMap(ProjectAndJob::getJobName, ProjectAndJob::getId));
  }

  @Transactional
  @Override
  public void analyzeProject(String projectId, MultipartFile multipartFile) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    project.setAnalysisStatus(Project.AnalysisStatus.PROCEEDING);
    projectRepository.save(project);

    projectAnalysisService
        .analyzeProject(projectId, multipartFile)
        .exceptionally(
            ex -> {
              log.error("FP 분석 실패", ex);
              project.setAnalysisStatus(Project.AnalysisStatus.FAILED);
              projectRepository.save(project);
              return null;
            });
  }

  private void createDeveloperWorkRecords(String projectCode) {
    // 1. 프로젝트 직무 조회
    List<ProjectAndJob> jobs = projectAndJobRepository.findByProjectCode(projectCode);

    for (ProjectAndJob job : jobs) {
      // 2. 각 직무에 할당된 개발자 목록 조회
      List<String> developerEmpIds = fetchDevelopersForJob(job.getId()); // 사번 목록

      for (String empId : developerEmpIds) {
        DeveloperProjectWork work =
            DeveloperProjectWork.builder()
                .employeeIdentificationNumber(empId)
                .projectCode(projectCode)
                .approvalStatus(DeveloperProjectWork.ApprovalStatus.NOT_REQUESTED)
                .build();
        developerProjectWorkRepository.save(work);
      }
    }
  }

  private List<String> fetchDevelopersForJob(Long projectJobId) {
    return squadEmployeeCommandRepository.findByProjectAndJobId(projectJobId).stream()
        .map(SquadEmployee::getEmployeeIdentificationNumber)
        .toList();
  }
}
