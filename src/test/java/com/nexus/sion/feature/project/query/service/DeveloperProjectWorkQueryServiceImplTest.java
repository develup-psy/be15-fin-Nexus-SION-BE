package com.nexus.sion.feature.project.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;
import com.nexus.sion.feature.project.query.dto.request.WorkRequestQueryDto;
import com.nexus.sion.feature.project.query.dto.response.FunctionTypeDto;
import com.nexus.sion.feature.project.query.dto.response.ProjectInfoDto;
import com.nexus.sion.feature.project.query.dto.response.WorkInfoQueryDto;
import com.nexus.sion.feature.project.query.repository.DeveloperProjectWorkQueryRepository;
import com.nexus.sion.feature.project.query.repository.ProjectQueryRepository;

class DeveloperProjectWorkQueryServiceImplTest {

  private DeveloperProjectWorkQueryRepository workQueryRepository;
  private ProjectQueryRepository projectQueryRepository;
  private DeveloperProjectWorkQueryServiceImpl service;

  @BeforeEach
  void setUp() {
    workQueryRepository = mock(DeveloperProjectWorkQueryRepository.class);
    projectQueryRepository = mock(ProjectQueryRepository.class);
    service = new DeveloperProjectWorkQueryServiceImpl(projectQueryRepository, workQueryRepository);
  }

  @Test
  @DisplayName("관리자용 작업 요청 목록 조회")
  void getRequestsForAdmin() {
    // given
    String status = "PENDING";
    int page = 0, size = 10;
    List<WorkRequestQueryDto> mockList = List.of(new WorkRequestQueryDto());
    when(workQueryRepository.findForAdmin(status)).thenReturn(mockList);
    when(workQueryRepository.getTotalCountForAdmin(status)).thenReturn(1L);

    // when
    PageResponse<WorkRequestQueryDto> result = service.getRequestsForAdmin(status, page, size);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    verify(workQueryRepository).findForAdmin(status);
    verify(workQueryRepository).getTotalCountForAdmin(status);
  }

  @Test
  @DisplayName("사번 기준 작업 요청 목록 조회")
  void getRequestsByEmployeeId() {
    // given
    String employeeId = "EMP001";
    int page = 0, size = 10;
    List<WorkRequestQueryDto> mockList =
        List.of(new WorkRequestQueryDto(), new WorkRequestQueryDto());
    when(workQueryRepository.findByEmployeeId(employeeId)).thenReturn(mockList);

    // when
    PageResponse<WorkRequestQueryDto> result =
        service.getRequestsByEmployeeId(employeeId, page, size);

    // then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    verify(workQueryRepository).findByEmployeeId(employeeId);
  }

  @Test
  @DisplayName("프로젝트 정보 조회")
  void getProjectInfo() {
    // given
    Long workId = 1L;
    ProjectInfoDto dto =
        new ProjectInfoDto(
            "PROJ001",
            "테스트 프로젝트",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 6, 30),
            DeveloperProjectWork.ApprovalStatus.PENDING);
    when(projectQueryRepository.findProjectInfoByWorkId(workId)).thenReturn(dto);

    // when
    ProjectInfoDto result = service.getProjectInfo(workId);

    // then
    assertThat(result.getProjectCode()).isEqualTo("PROJ001");
    assertThat(result.getProjectTitle()).isEqualTo("테스트 프로젝트");
    assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 6, 30));
    assertThat(result.getApprovalStatus()).isEqualTo(DeveloperProjectWork.ApprovalStatus.PENDING);
    verify(projectQueryRepository).findProjectInfoByWorkId(workId);
  }

  @Test
  @DisplayName("작업 요청 상세 조회")
  void getRequestDetailById() {
    // given
    Long projectWorkId = 2L;
    WorkInfoQueryDto dto = new WorkInfoQueryDto();
    when(projectQueryRepository.findById(projectWorkId)).thenReturn(dto);

    // when
    WorkInfoQueryDto result = service.getRequestDetailById(projectWorkId);

    // then
    assertThat(result).isNotNull();
    verify(projectQueryRepository).findById(projectWorkId);
  }

  @Test
  @DisplayName("기능유형 목록 반환")
  void getFunctionTypes() {
    // when
    List<FunctionTypeDto> result = service.getFunctionTypes();

    // then
    List<String> expected = List.of("EI", "EO", "EQ", "ILF", "EIF");
    List<String> actual = result.stream().map(FunctionTypeDto::getName).toList();

    assertThat(actual).containsExactlyElementsOf(expected);
  }
}
