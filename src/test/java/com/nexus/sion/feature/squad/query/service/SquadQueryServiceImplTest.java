package com.nexus.sion.feature.squad.query.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.*;
import com.nexus.sion.feature.squad.query.mapper.SquadQueryMapper;
import com.nexus.sion.feature.squad.query.repository.SquadQueryRepository;
import com.nexus.sion.feature.squad.query.util.CalculateSquad;

class SquadQueryServiceImplTest {

  @InjectMocks private SquadQueryServiceImpl squadQueryService;

  @Mock private SquadQueryRepository squadQueryRepository;

  @Mock private SquadQueryMapper squadQueryMapper;

  @Mock private CalculateSquad calculateSquad;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("스쿼드 목록 조회 성공")
  void getSquads_success() {
    // given
    SquadListRequest request = new SquadListRequest("PJT001", 0, 10);
    SquadListResponse dummy =
        new SquadListResponse(
            "SQD001", "백엔드팀", false, List.of(), "2024-01-01 ~ 2024-04-01", "₩2,000,000");
    PageResponse<SquadListResponse> response = PageResponse.fromJooq(List.of(dummy), 1, 0, 10);
    given(squadQueryRepository.findSquads(request)).willReturn(response);

    // when
    PageResponse<SquadListResponse> result = squadQueryService.getSquads(request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(squadQueryRepository).findSquads(request);
  }

  @Test
  @DisplayName("역할별 후보 개발자 조회 성공")
  void findCandidatesByRoles_success() {
    // given
    String projectId = "PJT001";
    JobInfo job1 = new JobInfo(1L, "프론트엔드");

    DeveloperSummary dev =
        DeveloperSummary.builder()
            .id("DEV001")
            .name("홍길동")
            .grade("MID")
            .avgTechScore(90.0)
            .domainCount(3)
            .weight(0.8)
            .monthlyUnitPrice(500000)
            .productivity(BigDecimal.valueOf(1.2))
            .build();

    given(squadQueryMapper.findJobsByProjectId(projectId)).willReturn(List.of(job1));
    given(squadQueryMapper.findDevelopersByStacksPerJob(1L, projectId))
        .willReturn(new ArrayList<>(List.of(dev)));

    // when
    SquadCandidateResponse result = squadQueryService.findCandidatesByRoles(projectId);

    // then
    assertThat(result.candidates()).containsKey("프론트엔드");
    assertThat(result.candidates().get("프론트엔드")).hasSize(1);
    verify(squadQueryMapper).findJobsByProjectId(projectId);
    verify(squadQueryMapper).findDevelopersByStacksPerJob(1L, projectId);
    verify(calculateSquad).applyWeightToCandidates(anyMap());
  }

  @Test
  @DisplayName("역할별 필요 인원 수 조회 성공")
  void findRequiredMemberCountByRoles_success() {
    // given
    String projectId = "PJT001";
    JobAndCount entry = new JobAndCount("백엔드", 2);
    given(squadQueryMapper.findRequiredMemberCountByRoles(projectId)).willReturn(List.of(entry));

    // when
    Map<String, Integer> result = squadQueryService.findRequiredMemberCountByRoles(projectId);

    // then
    assertThat(result).containsEntry("백엔드", 2);
    verify(squadQueryMapper).findRequiredMemberCountByRoles(projectId);
  }

  @Test
  @DisplayName("스쿼드 상세 조회 성공")
  void getSquadDetailByCode_success() {
    // given
    String squadCode = "SQD001";
    SquadDetailResponse response = new SquadDetailResponse();
    given(squadQueryRepository.fetchSquadDetail(squadCode)).willReturn(response);

    // when
    SquadDetailResponse result = squadQueryService.getSquadDetailByCode(squadCode);

    // then
    assertThat(result).isNotNull();
    verify(squadQueryRepository).fetchSquadDetail(squadCode);
  }

  @Test
  @DisplayName("스쿼드 상세 조회 실패 - 존재하지 않음")
  void getSquadDetailByCode_fail_notFound() {
    // given
    String squadCode = "INVALID_CODE";
    given(squadQueryRepository.fetchSquadDetail(squadCode))
        .willThrow(new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> squadQueryService.getSquadDetailByCode(squadCode))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.SQUAD_DETAIL_NOT_FOUND.getMessage());

    verify(squadQueryRepository).fetchSquadDetail(squadCode);
  }

  @Test
  @DisplayName("개발자 조회 실패 - 직무 없음")
  void findCandidatesByRoles_fail_whenJobListEmpty() {
    // given
    String projectId = "PJT999";
    given(squadQueryMapper.findJobsByProjectId(projectId)).willReturn(Collections.emptyList());

    // when
    SquadCandidateResponse result = squadQueryService.findCandidatesByRoles(projectId);

    // then
    assertThat(result.candidates()).isEmpty();
    verify(calculateSquad).applyWeightToCandidates(result.candidates());
  }
}
