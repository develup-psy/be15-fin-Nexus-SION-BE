package com.nexus.sion.feature.squad.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;
import com.nexus.sion.feature.squad.query.repository.SquadQueryRepository;

class SquadQueryServiceImplTest {

  private SquadQueryRepository squadQueryRepository;
  private SquadQueryServiceImpl squadQueryService;

  @BeforeEach
  void setUp() {
    squadQueryRepository = Mockito.mock(SquadQueryRepository.class);
    squadQueryService = new SquadQueryServiceImpl(squadQueryRepository);
  }

  @Test
  @DisplayName("스쿼드 목록을 정상적으로 조회한다")
  void findSquads_returnsSquadList() {
    // given
    SquadListRequest request = new SquadListRequest("PROJ-001");

    SquadListResponse.MemberInfo member = new SquadListResponse.MemberInfo("홍길동", "백엔드");
    SquadListResponse squad =
        new SquadListResponse(
            "SQD-1", // squadCode
            "백엔드팀", // squadName
            false, // isAiRecommended
            List.of(member), // members
            "2024-01-01 ~ 2024-04-01", // estimatedPeriod
            "₩3,000,000" // estimatedCost
            );

    List<SquadListResponse> mockResponse = List.of(squad);
    when(squadQueryRepository.findSquads(request)).thenReturn(mockResponse);

    // when
    List<SquadListResponse> result = squadQueryService.findSquads(request);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getSquadCode()).isEqualTo("SQD-1");
    assertThat(result.get(0).getMembers()).hasSize(1);
    verify(squadQueryRepository, times(1)).findSquads(request);
  }

  @Test
  @DisplayName("스쿼드 목록이 없으면 예외를 던진다")
  void findSquads_throwsException_whenNoSquadsFound() {
    // given
    SquadListRequest request = new SquadListRequest("PROJ-001");
    when(squadQueryRepository.findSquads(request)).thenReturn(List.of()); // 빈 리스트 반환

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
            "직접 구성됨");

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
