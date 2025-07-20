package com.nexus.sion.feature.project.query.service;

import java.util.List;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.WorkRequestQueryDto;
import com.nexus.sion.feature.project.query.dto.response.DeveloperApprovalResponse;
import com.nexus.sion.feature.project.query.dto.response.FunctionTypeDto;
import com.nexus.sion.feature.project.query.dto.response.ProjectInfoDto;
import com.nexus.sion.feature.project.query.dto.response.WorkInfoQueryDto;

public interface DeveloperProjectWorkQueryService {
  PageResponse<WorkRequestQueryDto> getRequestsForAdmin(String status, int page, int size);

  PageResponse<WorkRequestQueryDto> getRequestsByEmployeeId(
      String employeeId, String status, int page, int size);

  ProjectInfoDto getProjectInfo(Long workId);

  WorkInfoQueryDto getRequestDetailById(Long projectWorkId);

  List<FunctionTypeDto> getFunctionTypes();

  List<DeveloperApprovalResponse> getDeveloperApprovals(String projectCode);
}
