package com.nexus.sion.feature.project.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.jooq.generated.enums.ProjectAnalysisStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectDetailResponse;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;
import com.nexus.sion.feature.project.query.repository.ProjectQueryRepository;

@ExtendWith(MockitoExtension.class)
class ProjectQueryServiceImplTest {

  @Mock private ProjectQueryRepository repository;
  @InjectMocks private ProjectQueryServiceImpl service;

  @Test
  @DisplayName("예산 조건으로 프로젝트 목록을 조회한다")
  void findProjects_withBudgetFilter_returnsCorrectProjects() {
    ProjectListRequest request = new ProjectListRequest();
    request.setPage(0);
    request.setSize(10);
    request.setMaxBudget(10000000L);

    ProjectListResponse project =
        new ProjectListResponse(
            "PROJ-001",
            "AI 기반 리포트 시스템",
            "보고서 자동 생성",
            "2025-03-01",
            "2025-06-01",
            4,
            "COMPLETE",
            "HR시스템",
            5,
            ProjectAnalysisStatus.COMPLETE);

    PageResponse<ProjectListResponse> mockPage = PageResponse.fromJooq(List.of(project), 1, 0, 10);

    when(repository.findProjects(request)).thenReturn(mockPage);

    PageResponse<ProjectListResponse> result = service.findProjects(request);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("AI 기반 리포트 시스템");
    verify(repository).findProjects(request);
  }

  @Test
  @DisplayName("상태 필터를 적용하여 프로젝트를 조회한다")
  void findProjects_withStatusFilter() {
    ProjectListRequest request = new ProjectListRequest();
    request.setStatuses(List.of("WAITING", "IN_PROGRESS"));
    request.setPage(0);
    request.setSize(5);

    ProjectListResponse project =
        new ProjectListResponse(
            "PROJ-002",
            "웹 리뉴얼",
            "프론트엔드 개선",
            "2025-02-01 ~ 2025-05-01",
            "WAITING",
            3,
            "COMPLETE",
            "웹",
            5,
            ProjectAnalysisStatus.COMPLETE);

    PageResponse<ProjectListResponse> mockPage = PageResponse.fromJooq(List.of(project), 1, 0, 5);

    when(repository.findProjects(request)).thenReturn(mockPage);

    PageResponse<ProjectListResponse> result = service.findProjects(request);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getStatus()).isEqualTo("COMPLETE");
    verify(repository).findProjects(request);
  }

  @Test
  @DisplayName("키워드 검색 조건과 다른 필터를 함께 적용한다")
  void findProjects_withKeywordAndOtherFilters() {
    ProjectListRequest request = new ProjectListRequest();
    request.setKeyword("AI");
    request.setMaxNumberOfMembers(10);
    request.setMaxBudget(20000000L);
    request.setPage(0);
    request.setSize(5);

    ProjectListResponse project =
        new ProjectListResponse(
            "PROJ-003",
            "AI 기반 리포트 시스템",
            "보고서 자동 생성",
            "2025-03-01",
            "2025-06-01",
            4,
            "COMPLETE",
            "HR시스템",
            5,
            ProjectAnalysisStatus.COMPLETE);

    PageResponse<ProjectListResponse> mockPage = PageResponse.fromJooq(List.of(project), 1, 0, 5);

    when(repository.findProjects(request)).thenReturn(mockPage);

    PageResponse<ProjectListResponse> result = service.findProjects(request);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).contains("AI");
    assertThat(result.getContent().get(0).getHrCount()).isLessThanOrEqualTo(10);
    verify(repository).findProjects(request);
  }

  @Test
  @DisplayName("프로젝트 상세 정보를 정상적으로 조회한다")
  void getProjectDetail_success() {
    String projectCode = "PROJ-001";
    List<String> techStacks = List.of("Java", "Spring");
    List<ProjectDetailResponse.SquadMemberInfo> members =
        List.of(
            new ProjectDetailResponse.SquadMemberInfo(
                "EMP001", 1, "https://img.com/leader.jpg", "홍길동", "백엔드"),
            new ProjectDetailResponse.SquadMemberInfo(
                "EMP002", 0, "https://img.com/user.jpg", "이몽룡", "프론트엔드"));

    ProjectDetailResponse mockResponse =
        new ProjectDetailResponse(
            "AI 리포트 시스템",
            "인공지능",
            "https://spec.com/123",
            "AI 기반 자동 보고 시스템",
            "2025-01-01 ~ 2025-03-01",
            "₩5,000,000",
            techStacks,
            members,
            "WAITING",
            ProjectAnalysisStatus.COMPLETE,
                "ha_1_1_1");

    when(repository.getProjectDetail(projectCode)).thenReturn(mockResponse);

    ProjectDetailResponse result = service.getProjectDetail(projectCode);

    assertThat(result.getTitle()).isEqualTo("AI 리포트 시스템");
    assertThat(result.getDomainName()).isEqualTo("인공지능");
    assertThat(result.getTechStacks()).contains("Java");
    assertThat(result.getMembers()).hasSize(2);
    assertThat(result.getMembers().get(0).getIsLeader()).isEqualTo(1);

    verify(repository).getProjectDetail(projectCode);
  }

  @Test
  @DisplayName("존재하지 않는 프로젝트 코드를 조회하면 예외가 발생한다")
  void getProjectDetail_notFound_throwsException() {
    String invalidCode = "INVALID";
    when(repository.getProjectDetail(invalidCode))
        .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    try {
      service.getProjectDetail(invalidCode);
    } catch (BusinessException e) {
      assertThat(e.getErrorCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    verify(repository).getProjectDetail(invalidCode);
  }
}
