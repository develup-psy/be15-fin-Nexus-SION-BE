package com.nexus.sion.feature.project.command.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.application.dto.request.SquadReplacementRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectRegisterRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ProjectUpdateRequest;
import com.nexus.sion.feature.project.command.application.dto.response.ProjectRegisterResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.domain.repository.*;
import com.nexus.sion.feature.project.command.domain.service.ProjectAnalysisService;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkRepository;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
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
  private final NotificationCommandService notificationCommandService;
  private final SquadCommandRepository squadCommandRepository;
  private final SquadEmployeeCommandRepository squadEmployeeCommandRepository;

  private final ProjectFunctionEstimateRepository projectFunctionEstimateRepository;
  private final ProjectFpSummaryRepository projectFpSummaryRepository;

  private final DeveloperProjectWorkRepository developerProjectWorkRepository;
  private final MemberRepository memberRepository;

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
  public void updateProject(ProjectUpdateRequest request) {
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
    project.setRequestSpecificationUrl(request.getRequestSpecificationUrl());

    projectCommandRepository.save(project);
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
      createDeveloperProjectWorks(projectCode);
    } else {
      project.setActualEndDate(null);
    }
    projectCommandRepository.save(project);
  }

  private String findActiveSquadCode(String projectCode) {
    return squadCommandRepository
            .findByProjectCodeAndIsActiveIsTrue(projectCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND))
            .getSquadCode();
  }

  private List<SquadEmployee> findSquadEmployees(String squadCode) {
    return squadEmployeeCommandRepository.findBySquadCode(squadCode);
  }

  private void sendTaskUploadRequestNotification(SquadEmployee employee, Long developerProjectWorkId) {
    String employeeId = employee.getEmployeeIdentificationNumber();
    notificationCommandService.createAndSendNotification(
            null,
            employeeId,
            null,
            NotificationType.TASK_UPLOAD_REQUEST,
            developerProjectWorkId.toString()
    );
  }


  @Override
  public Map<String, Long> findProjectAndJobIdMap(String projectId) {
    return projectAndJobRepository.findByProjectCode(projectId).stream()
            .collect(Collectors.toMap(ProjectAndJob::getJobName, ProjectAndJob::getId));
  }

  @Transactional
  @Override
  public void analyzeProject(
          String projectId, MultipartFile multipartFile, String employeeIdentificationNumber) {
    // 기존에 project_fp_summary나 project_function_estimate가 있다면 삭제
    ProjectFpSummary fpSummary =
            projectFpSummaryRepository
                    .findByProjectCode(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    projectFunctionEstimateRepository.deleteByProjectFpSummaryId(fpSummary.getId());
    projectFpSummaryRepository.deleteByProjectCode(projectId);

    Project project =
            projectRepository
                    .findById(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    project.setAnalysisStatus(Project.AnalysisStatus.PROCEEDING);
    projectRepository.save(project);

    projectAnalysisService
            .analyzeProject(projectId, multipartFile, employeeIdentificationNumber)
            .exceptionally(
                    ex -> {
                      log.error("FP 분석 실패", ex);
                      project.setAnalysisStatus(Project.AnalysisStatus.FAILED);
                      projectRepository.save(project);
                      // 분석 실패 알림
                      notifyFPAnalysisFailure(employeeIdentificationNumber, projectId);
                      return null;
                    });
  }

  private void notifyFPAnalysisFailure(String managerId, String projectId) {
    notificationCommandService.createAndSendNotification(
            null, managerId, null, NotificationType.FP_ANALYSIS_FAILURE, projectId);
  }

  private void createDeveloperProjectWorks(String projectCode) {
    List<SquadEmployee> employees = squadEmployeeCommandRepository.findByProjectCode(projectCode);

    for (SquadEmployee employee : employees) {
      DeveloperProjectWork dpw =
              DeveloperProjectWork.builder()
                      .employeeIdentificationNumber(employee.getEmployeeIdentificationNumber())
                      .projectCode(projectCode)
                      .approvalStatus(DeveloperProjectWork.ApprovalStatus.NOT_REQUESTED)
                      .build();

      DeveloperProjectWork saved = developerProjectWorkRepository.save(dpw);

      sendTaskUploadRequestNotification(employee, saved.getId());
    }
  }

  @Override
  public void replaceMember(SquadReplacementRequest request) {

    Squad existSquad = squadCommandRepository.findById(request.getSquadCode()).orElseThrow(
            () -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND)
    );


    SquadEmployee existsMember = squadEmployeeCommandRepository.findBySquadCodeAndEmployeeIdentificationNumber(
            request.getSquadCode(), request.getOldEmployeeId()).orElseThrow(
            () -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND)
    );

    if(existsMember.isLeader()){
      throw new BusinessException(ErrorCode.INVALID_LEADER_REPLACEMENT);
    }

    squadEmployeeCommandRepository.deleteBySquadCodeAndEmployeeIdentificationNumber(
            request.getSquadCode(),
            request.getOldEmployeeId()
    );

    boolean existsNew = squadEmployeeCommandRepository.existsBySquadCodeAndEmployeeIdentificationNumber(
            request.getSquadCode(), request.getNewEmployeeId());

    if (existsNew) {
      throw new BusinessException(ErrorCode.INVALID_EXIST_MEMBER_REPLACEMENT);
    }

    Member targetMember = memberRepository.findById(request.getNewEmployeeId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    if (targetMember.getStatus() != MemberStatus.AVAILABLE) {
      throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
    }

    SquadEmployee newMember = SquadEmployee.builder()
            .squadCode(request.getSquadCode())
            .employeeIdentificationNumber(request.getNewEmployeeId())
            .projectAndJobId(existsMember.getProjectAndJobId())
            .isLeader(false)
            .build();

    squadEmployeeCommandRepository.save(newMember);
  }

}