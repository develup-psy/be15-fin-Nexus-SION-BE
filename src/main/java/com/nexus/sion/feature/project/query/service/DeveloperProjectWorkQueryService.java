package com.nexus.sion.feature.project.query.service;

import java.util.List;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.response.FunctionTypeDto;
import com.nexus.sion.feature.project.query.dto.response.ProjectInfoDto;
import com.nexus.sion.feature.project.query.dto.response.WorkInfoQueryDto;
import com.nexus.sion.feature.project.query.dto.response.WorkRequestQueryDto;

public interface DeveloperProjectWorkQueryService {
  List<WorkRequestQueryDto> getAllRequests();

  PageResponse<WorkRequestQueryDto> getRequestsByEmployeeId(String employeeId, int page, int size);

  ProjectInfoDto getProjectInfo(Long workId);

  WorkInfoQueryDto getRequestDetailById(Long projectWorkId);

  List<FunctionTypeDto> getFunctionTypes();
}
