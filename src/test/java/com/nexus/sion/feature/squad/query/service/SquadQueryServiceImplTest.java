package com.nexus.sion.feature.squad.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
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
        SquadListResponse squad = new SquadListResponse(
                "SQD-1",                    // squadCode
                "백엔드팀",                 // squadName
                false,                      // isAiRecommended
                List.of(member),           // members
                "2024-01-01 ~ 2024-04-01", // estimatedPeriod
                "₩3,000,000"               // estimatedCost
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
}
