package com.nexus.sion.feature.squad.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.nexus.sion.feature.squad.query.mapper.SquadQueryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResultResponse;
import com.nexus.sion.feature.squad.query.repository.SquadQueryRepository;

class SquadQueryServiceImplTest {

  private SquadQueryRepository squadQueryRepository;
  private SquadQueryServiceImpl squadQueryService;
  private SquadQueryMapper squadQueryMapper;

  @BeforeEach
  void setUp() {
    squadQueryRepository = Mockito.mock(SquadQueryRepository.class);
    squadQueryService = new SquadQueryServiceImpl(squadQueryRepository, squadQueryMapper);
  }

  @Test
  @DisplayName("스쿼드 목록을 정상적으로 조회한다")
  void findSquads_returnsSquadList() {
    // given
    SquadListRequest request = new SquadListRequest("ha_1_1", 0, 10);

    SquadListResponse.MemberInfo member = new SquadListResponse.MemberInfo("홍길동", "백엔드");
    SquadListResponse squad =
        new SquadListResponse(
            "SQD-1", "백엔드팀", false, List.of(member), "2024-01-01 ~ 2024-04-01", "₩3,000,000");

    List<SquadListResponse> content = List.of(squad);
    SquadListResultResponse mockResult = new SquadListResultResponse(content, 1, 0, 10);

    when(squadQueryRepository.findSquads(request)).thenReturn(mockResult);

    // when
    SquadListResultResponse result = squadQueryService.findSquads(request);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getSquadCode()).isEqualTo("SQD-1");
    assertThat(result.getContent().get(0).getMembers()).hasSize(1);
    verify(squadQueryRepository, times(1)).findSquads(request);
  }

  @Test
  @DisplayName("스쿼드 목록이 없으면 예외를 던진다")
  void findSquads_throwsException_whenNoSquadsFound() {
    // given
    SquadListRequest request = new SquadListRequest("ha_1_1", 0, 10);
    SquadListResultResponse emptyResult = new SquadListResultResponse(List.of(), 0, 0, 10);
    when(squadQueryRepository.findSquads(request)).thenReturn(emptyResult);

    // when & then
    assertThatThrownBy(() -> squadQueryService.findSquads(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.PROJECT_SQUAD_NOT_FOUND.getMessage());

    verify(squadQueryRepository, times(1)).findSquads(request);
  }

  @Test
  @DisplayName("스쿼드 상세정보를 정상적으로 반환한다")
  void getSquadDetailByCode_success() {
    // given
    String squadCode = "SQD-001";

    SquadDetailResponse.MemberInfo member =
        new SquadDetailResponse.MemberInfo(true, "https://img.com/p1.jpg", "백엔드", "홍길동");

    SquadDetailResponse.CostBreakdown cost =
        new SquadDetailResponse.CostBreakdown("홍길동", "백엔드", "A", "₩2,000,000");

    SquadDetailResponse.SummaryInfo summary =
        new SquadDetailResponse.SummaryInfo(Map.of("백엔드", 1L), Map.of("A", 1L));

    SquadDetailResponse.CommentResponse comment =
        new SquadDetailResponse.CommentResponse(
            1L, "EMP001", "홍길동", LocalDateTime.of(2025, 6, 24, 12, 0));

    SquadDetailResponse mockResponse =
        new SquadDetailResponse(
            squadCode,
            "백엔드팀",
            true,
            "3개월",
            "₩2,000,000",
            summary,
            List.of("Java", "Spring"),
            List.of(member),
            List.of(cost),
            "직접 구성됨",
            List.of(comment));

    when(squadQueryRepository.findSquadDetailByCode(squadCode)).thenReturn(mockResponse);

    // when
    SquadDetailResponse result = squadQueryService.getSquadDetailByCode(squadCode);

    // then
    assertThat(result.getSquadCode()).isEqualTo(squadCode);
    assertThat(result.getMembers()).hasSize(1);
    assertThat(result.getTechStacks()).contains("Java");
    verify(squadQueryRepository, times(1)).findSquadDetailByCode(squadCode);
  }

  @Test
  @DisplayName("스쿼드 코드가 존재하지 않으면 예외를 던진다")
  void getSquadDetailByCode_notFound_throwsException() {
    // given
    String squadCode = "INVALID";

    when(squadQueryRepository.findSquadDetailByCode(squadCode))
        .thenThrow(new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND));

    // when & then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              squadQueryService.getSquadDetailByCode(squadCode);
            });

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SQUAD_DETAIL_NOT_FOUND);
    verify(squadQueryRepository, times(1)).findSquadDetailByCode(squadCode);
  }
}
