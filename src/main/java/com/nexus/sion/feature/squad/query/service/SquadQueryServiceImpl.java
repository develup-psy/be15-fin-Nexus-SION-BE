package com.nexus.sion.feature.squad.query.service;

import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResultResponse;
import com.nexus.sion.feature.squad.query.repository.SquadQueryRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SquadQueryServiceImpl implements SquadQueryService {

  private final SquadQueryRepository squadQueryRepository;
  private final ProjectRepository projectRepository;


  @Override
  public SquadListResultResponse findSquads(SquadListRequest request) {
    SquadListResultResponse result = squadQueryRepository.findSquads(request);
    return result != null ? result : new SquadListResultResponse(List.of(), request.getPage(), request.getSize(), 0L);
  }

  @Override
  public SquadDetailResponse getSquadDetailByCode(String squadCode) {
    SquadDetailResponse response = squadQueryRepository.findSquadDetailByCode(squadCode);

    if (response == null) {
      throw new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND);
    }

    return response;
  }

  public Object findSquadsOrConfirmed(SquadListRequest request) {
    String projectCode = request.getProjectCode();

    Project project = projectRepository.findById(projectCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // ✅ 종료된 프로젝트면 무조건 확정된 스쿼드만 보여주기
    if (project.getStatus() == Project.ProjectStatus.COMPLETE
            || project.getStatus() == Project.ProjectStatus.INCOMPLETE) {

      SquadDetailResponse confirmed = getConfirmedSquadByProjectCode(projectCode);
      return confirmed;
    }

    // ✅ 진행 중 프로젝트인 경우
    if (hasConfirmedSquad(projectCode)) {
      return getConfirmedSquadByProjectCode(projectCode);
    }

    return findSquads(request);
  }


  @Override
  public boolean hasConfirmedSquad(String projectCode) {
    return squadQueryRepository.existsByProjectCodeAndIsActive(projectCode);
  }

  @Override
  public SquadDetailResponse getConfirmedSquadByProjectCode(String projectCode) {
    return squadQueryRepository.findConfirmedSquadByProjectCode(projectCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND));
  }
}