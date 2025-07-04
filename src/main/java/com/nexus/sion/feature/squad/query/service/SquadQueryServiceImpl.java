package com.nexus.sion.feature.squad.query.service;

import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nexus.sion.common.dto.PageResponse;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.*;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.mapper.SquadQueryMapper;
import com.nexus.sion.feature.squad.query.repository.SquadQueryRepository;
import com.nexus.sion.feature.squad.query.util.CalculateSquad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SquadQueryServiceImpl implements SquadQueryService {

  private final SquadQueryRepository squadQueryRepository;
  private final SquadQueryMapper squadQueryMapper;
  private final CalculateSquad calculateSquad;
  private final ProjectRepository projectRepository;

  @Override
  public SquadDetailResponse getSquadDetailByCode(String squadCode) {
    SquadDetailResponse response = squadQueryRepository.findSquadDetailByCode(squadCode);

    if (response == null) {
      throw new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND);
    }

    return response;
  }

  @Override
  public Object findSquadsOrConfirmed(SquadListRequest request) {
    String projectCode = request.getProjectCode();
    Project project = getProjectOrThrow(projectCode);

    // ✅ 종료된 프로젝트면 무조건 확정된 스쿼드만 보여주기
    if (isProjectComplete(project)) {
      return getConfirmedSquadIfExistsOrThrow(projectCode);
    }

    if (hasConfirmedSquad(projectCode)) {
      return getConfirmedSquadIfExistsOrThrow(projectCode);
    }

    return squadQueryRepository.findSquads(request);
  }

  private Project getProjectOrThrow(String projectCode) {
    return projectRepository
        .findById(projectCode)
        .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
  }

  private boolean isProjectComplete(Project project) {
    return project.getStatus() == Project.ProjectStatus.COMPLETE
        || project.getStatus() == Project.ProjectStatus.INCOMPLETE;
  }

  private SquadDetailResponse getConfirmedSquadIfExistsOrThrow(String projectCode) {
    return squadQueryRepository.findConfirmedSquadByProjectCode(projectCode);
  }

  @Override
  public boolean hasConfirmedSquad(String projectCode) {
    return squadQueryRepository.existsByProjectCodeAndIsActive(projectCode);
  }

  @Override
  public PageResponse<SquadListResponse> findSquads(SquadListRequest request) {
    return squadQueryRepository.findSquads(request);
  }

  @Override
  public SquadDetailResponse getConfirmedSquadByProjectCode(String projectCode) {
    return squadQueryRepository.findConfirmedSquadByProjectCode(projectCode);
  }

  @Override
  public SquadCandidateResponse findCandidatesByRoles(String projectId) {
    List<JobInfo> jobList = squadQueryMapper.findJobsByProjectId(projectId);

    Map<String, List<DeveloperSummary>> result = new LinkedHashMap<>();

    for (JobInfo job : jobList) {
      List<DeveloperSummary> developers =
          squadQueryMapper.findDevelopersByStacksPerJob(job.getProjectAndJobId(), projectId);
      result.put(job.getJobName(), developers);
    }

    calculateSquad.applyWeightToCandidates(result);
    for (List<DeveloperSummary> list : result.values()) {
      list.sort(Comparator.comparingDouble(DeveloperSummary::getWeight).reversed());
    }

    return new SquadCandidateResponse(result);
  }

  @Override
  public Map<String, Integer> findRequiredMemberCountByRoles(String projectId) {
    List<JobAndCount> result = squadQueryMapper.findRequiredMemberCountByRoles(projectId);
    return result.stream()
        .collect(Collectors.toMap(JobAndCount::getJobName, JobAndCount::getRequiredNumber));
  }
}
