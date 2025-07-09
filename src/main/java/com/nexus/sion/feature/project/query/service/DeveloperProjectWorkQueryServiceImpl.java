package com.nexus.sion.feature.project.query.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistory;
import com.nexus.sion.feature.project.query.dto.response.FunctionTypeDto;
import com.nexus.sion.feature.project.query.dto.response.ProjectInfoDto;
import com.nexus.sion.feature.project.query.dto.response.WorkInfoQueryDto;
import com.nexus.sion.feature.project.query.dto.response.WorkRequestQueryDto;
import com.nexus.sion.feature.project.query.repository.DeveloperProjectWorkQueryRepository;
import com.nexus.sion.feature.project.query.repository.ProjectQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeveloperProjectWorkQueryServiceImpl implements DeveloperProjectWorkQueryService {

  private final ProjectQueryRepository projectQueryRepository;
  private final DeveloperProjectWorkQueryRepository developerProjectWorkQueryRepository;

  @Override
  public List<WorkRequestQueryDto> getAllRequests() {
    return developerProjectWorkQueryRepository.findAll();
  }

  @Override
  public List<WorkRequestQueryDto> getRequestsByEmployeeId(String employeeId) {
    return developerProjectWorkQueryRepository.findByEmployeeId(employeeId);
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
