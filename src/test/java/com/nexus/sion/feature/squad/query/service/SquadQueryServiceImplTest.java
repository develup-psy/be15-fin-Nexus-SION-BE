package com.nexus.sion.feature.squad.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.aggregate.Project.ProjectStatus;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.*;
import com.nexus.sion.feature.squad.query.repository.SquadQueryRepository;

class SquadQueryServiceImplTest {

  private SquadQueryRepository squadQueryRepository;
  private SquadQueryServiceImpl squadQueryService;
  private ProjectRepository projectRepository;

  @BeforeEach
  void setUp() {
    squadQueryRepository = mock(SquadQueryRepository.class);
    projectRepository = mock(ProjectRepository.class);
    squadQueryService =
        new SquadQueryServiceImpl(squadQueryRepository, null, null, projectRepository);
  }

  @Test
  @DisplayName("진행 중 프로젝트이고 확정 스쿼드가 없으면 스쿼드 목록을 반환한다")
  void findSquadsOrConfirmed_shouldReturnSquadList_whenOngoingProjectAndNoConfirmed() {
    // given
    String projectCode = "PJT-001";
    SquadListRequest request = new SquadListRequest(projectCode, 0, 10);

    Project project =
        Project.builder()
            .projectCode(projectCode)
            .domainName("example.com")
            .description("예시 프로젝트입니다.")
            .title("백엔드 시스템 개발")
            .budget(5000000L)
            .status(ProjectStatus.IN_PROGRESS)
            .build();

    SquadListResponse squad =
        new SquadListResponse(
            "SQD-001", "백엔드팀", false, List.of(), "2024-01-01 ~ 2024-04-01", "₩2,000,000");
    PageResponse<SquadListResponse> mockList = PageResponse.fromJooq(List.of(squad), 1, 0, 10);

    when(projectRepository.findById(projectCode)).thenReturn(Optional.of(project));
    when(squadQueryRepository.existsByProjectCodeAndIsActive(projectCode)).thenReturn(false);
    when(squadQueryRepository.findSquads(request)).thenReturn(mockList);

    // when
    Object response = squadQueryService.findSquadsOrConfirmed(request);

    // then
    assertThat(response).isInstanceOf(PageResponse.class);
    PageResponse<SquadListResponse> result = (PageResponse<SquadListResponse>) response;
    assertThat(result.getContent()).hasSize(1);

    verify(projectRepository).findById(projectCode);
    verify(squadQueryRepository).existsByProjectCodeAndIsActive(projectCode);
    verify(squadQueryRepository).findSquads(request);
  }

  @Test
  @DisplayName("진행 중 프로젝트이고 확정 스쿼드가 있으면 상세 스쿼드를 반환한다")
  void findSquadsOrConfirmed_shouldReturnConfirmed_whenOngoingProjectAndConfirmedExists() {
    // given
    String projectCode = "PJT-001";
    SquadListRequest request = new SquadListRequest(projectCode, 0, 10);

    Project project =
        Project.builder()
            .projectCode(projectCode)
            .domainName("example.com")
            .description("예시 프로젝트입니다.")
            .title("백엔드 시스템 개발")
            .budget(5000000L)
            .status(ProjectStatus.IN_PROGRESS)
            .build();

    SquadDetailResponse mockDetail =
        new SquadDetailResponse(
            "SQD-001",
            "백엔드팀",
            true,
            "3개월",
            "₩2,000,000",
            new SquadDetailResponse.SummaryInfo(Map.of(), Map.of()),
            List.of("Java"),
            List.of(),
            List.of(),
            null,
            List.of());

    when(projectRepository.findById(projectCode)).thenReturn(Optional.of(project));
    when(squadQueryRepository.existsByProjectCodeAndIsActive(projectCode)).thenReturn(true);
    when(squadQueryRepository.findConfirmedSquadByProjectCode(projectCode)).thenReturn(mockDetail);

    // when
    Object response = squadQueryService.findSquadsOrConfirmed(request);

    // then
    assertThat(response).isInstanceOf(SquadDetailResponse.class);
    SquadDetailResponse result = (SquadDetailResponse) response;
    assertThat(result.getSquadCode()).isEqualTo("SQD-001");

    verify(projectRepository).findById(projectCode);
    verify(squadQueryRepository).existsByProjectCodeAndIsActive(projectCode);
    verify(squadQueryRepository).findConfirmedSquadByProjectCode(projectCode);
  }

  @Test
  @DisplayName("종료된 프로젝트이면 무조건 확정 스쿼드를 반환한다")
  void findSquadsOrConfirmed_shouldReturnConfirmed_whenProjectComplete() {
    // given
    String projectCode = "PJT-002";
    SquadListRequest request = new SquadListRequest(projectCode, 0, 10);

    Project project =
        Project.builder()
            .projectCode(projectCode)
            .domainName("example.com")
            .description("예시 프로젝트입니다.")
            .title("백엔드 시스템 개발")
            .budget(5000000L)
            .status(ProjectStatus.COMPLETE)
            .build();
    project.setStatus(ProjectStatus.COMPLETE);

    SquadDetailResponse mockDetail =
        new SquadDetailResponse(
            "SQD-002",
            "백엔드팀",
            true,
            "3개월",
            "₩2,000,000",
            new SquadDetailResponse.SummaryInfo(Map.of(), Map.of()),
            List.of("Spring"),
            List.of(),
            List.of(),
            null,
            List.of());

    when(projectRepository.findById(projectCode)).thenReturn(Optional.of(project));
    when(squadQueryRepository.findConfirmedSquadByProjectCode(projectCode)).thenReturn(mockDetail);

    // when
    Object response = squadQueryService.findSquadsOrConfirmed(request);

    // then
    assertThat(response).isInstanceOf(SquadDetailResponse.class);
    verify(projectRepository).findById(projectCode);
    verify(squadQueryRepository).findConfirmedSquadByProjectCode(projectCode);
  }

  @Test
  @DisplayName("종료된 프로젝트인데 확정 스쿼드가 없으면 예외 발생")
  void findSquadsOrConfirmed_shouldThrow_whenProjectCompleteAndNoConfirmed() {
    // given
    String projectCode = "PJT-002";
    SquadListRequest request = new SquadListRequest(projectCode, 0, 10);

    Project project =
        Project.builder()
            .projectCode(projectCode)
            .domainName("example.com")
            .description("예시 프로젝트입니다.")
            .title("백엔드 시스템 개발")
            .budget(5000000L)
            .status(ProjectStatus.COMPLETE)
            .build();

    when(projectRepository.findById(projectCode)).thenReturn(Optional.of(project));
    when(squadQueryRepository.findConfirmedSquadByProjectCode(projectCode))
        .thenThrow(new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> squadQueryService.findSquadsOrConfirmed(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.SQUAD_DETAIL_NOT_FOUND.getMessage());

    verify(projectRepository).findById(projectCode);
    verify(squadQueryRepository).findConfirmedSquadByProjectCode(projectCode);
  }
}
