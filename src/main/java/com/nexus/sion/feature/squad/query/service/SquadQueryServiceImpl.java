package com.nexus.sion.feature.squad.query.service;

import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexus.sion.common.dto.PageResponse;
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

  @Override
  public PageResponse<SquadListResponse> getSquads(SquadListRequest request) {
    return squadQueryRepository.findSquads(request);
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

  @Override
  public SquadDetailResponse getSquadDetailByCode(String squadCode) {
    return squadQueryRepository.fetchSquadDetail(squadCode);
  }
}
