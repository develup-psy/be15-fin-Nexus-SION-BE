package com.nexus.sion.feature.project.query.service;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.*;
import com.nexus.sion.feature.project.query.mapper.ProjectQueryMapper;
import com.nexus.sion.feature.project.query.repository.ProjectQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectQueryServiceImpl implements ProjectQueryService {

  private final ProjectQueryRepository projectQueryRepository;
  private final ProjectQueryMapper projectQueryMapper;

  @Override
  public PageResponse<ProjectListResponse> findProjects(ProjectListRequest request) {
    return projectQueryRepository.findProjects(request);
  }

  @Override
  public ProjectDetailResponse getProjectDetail(String projectCode) {
    return projectQueryRepository.getProjectDetail(projectCode);
  }

  @Override
  public ProjectForSquadResponse getProjectInfoForSquad(String projectCode) {
    ProjectForSquadResponse response = projectQueryMapper.findProjectInfo(projectCode);
    if (response == null) {
      throw new EntityNotFoundException("Project not found: " + projectCode);
    }

    List<JobRequirement> requirements = projectQueryMapper.findJobRequirements(projectCode);
    response.setJobRequirements(requirements);
    return response;
  }

  @Override
  public ProjectDetailResponse findProjectDetailByMemberIdAndProjectCode(
      String employeeId, String projectCode) {
    return projectQueryRepository.findProjectDetailByMemberIdAndProjectCode(
        employeeId, projectCode);
  }

  @Override
  public PageResponse<ProjectListResponse> findProjectListByMemberId(
      String employeeId, int page, int size) {
    return projectQueryRepository.findProjectListByMemberId(employeeId, page, size);
  }

  public PageResponse<ProjectListResponse> getProjectsByEmployeeId(
      String employeeId, List<String> statuses, int page, int size) {
    List<Project> pojos =
        projectQueryRepository.findProjectsByEmployeeId(employeeId, statuses, page, size);
    long totalCount = projectQueryRepository.countProjectsByEmployeeId(employeeId, statuses);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    List<ProjectListResponse> content =
        pojos.stream()
            .map(
                p ->
                    ProjectListResponse.builder()
                        .projectCode(p.getProjectCode())
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .startDate(
                            p.getStartDate() != null ? p.getStartDate().format(formatter) : null)
                        .endDate(
                            p.getExpectedEndDate() != null
                                ? p.getExpectedEndDate().format(formatter)
                                : null)
                        .period(
                            p.getStartDate() != null && p.getExpectedEndDate() != null
                                ? (int)
                                    p.getStartDate().until(p.getExpectedEndDate()).toTotalMonths()
                                : 0)
                        .status(p.getStatus() != null ? p.getStatus().name() : null)
                        .domainName(p.getDomainName())
                        .hrCount(p.getNumberOfMembers())
                        .analysisStatus(p.getAnalysisStatus())
                        .build())
            .toList();

    return PageResponse.fromJooq(content, totalCount, page, size);
  }
}
