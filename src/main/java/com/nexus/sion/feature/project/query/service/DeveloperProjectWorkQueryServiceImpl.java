package com.nexus.sion.feature.project.query.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistory;
import com.nexus.sion.feature.project.query.dto.request.WorkRequestQueryDto;
import com.nexus.sion.feature.project.query.dto.response.FunctionTypeDto;
import com.nexus.sion.feature.project.query.dto.response.ProjectInfoDto;
import com.nexus.sion.feature.project.query.dto.response.WorkInfoQueryDto;
import com.nexus.sion.feature.project.query.repository.DeveloperProjectWorkQueryRepository;
import com.nexus.sion.feature.project.query.repository.ProjectQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeveloperProjectWorkQueryServiceImpl implements DeveloperProjectWorkQueryService {

  private final ProjectQueryRepository projectQueryRepository;
  private final DeveloperProjectWorkQueryRepository developerProjectWorkQueryRepository;

  @Override
  public PageResponse<WorkRequestQueryDto> getRequestsForAdmin(String status, int page, int size) {
    List<WorkRequestQueryDto> result = developerProjectWorkQueryRepository.findForAdmin(status);
    long totalElements = developerProjectWorkQueryRepository.getTotalCountForAdmin(status);
    return PageResponse.fromJooq(result, totalElements, page, size);
  }

  @Override
  public PageResponse<WorkRequestQueryDto> getRequestsByEmployeeId(
      String employeeId, int page, int size) {
    List<WorkRequestQueryDto> fullList =
        developerProjectWorkQueryRepository.findByEmployeeId(employeeId);

    int total = fullList.size();
    int fromIndex = Math.min(page * size, total);
    int toIndex = Math.min(fromIndex + size, total);
    List<WorkRequestQueryDto> pagedList = fullList.subList(fromIndex, toIndex);

    return PageResponse.fromJooq(pagedList, total, page, size);
  }

  @Override
  public ProjectInfoDto getProjectInfo(Long workId) {
    return projectQueryRepository.findProjectInfoByWorkId(workId);
  }

  @Override
  public WorkInfoQueryDto getRequestDetailById(Long projectWorkId) {
    return projectQueryRepository.findById(projectWorkId);
  }

  @Override
  public List<FunctionTypeDto> getFunctionTypes() {
    return Arrays.stream(DeveloperProjectWorkHistory.FunctionType.values())
        .map(type -> new FunctionTypeDto(type.name(), type.name()))
        .collect(Collectors.toList());
  }
}
