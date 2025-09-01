package com.nexus.sion.feature.squad.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.service.ProjectCommandService;
import com.nexus.sion.feature.project.command.application.service.ProjectCommandServiceImpl;
import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.response.SquadRecommendationResponse;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
import com.nexus.sion.feature.squad.command.domain.service.*;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import com.nexus.sion.feature.squad.query.dto.response.SquadCandidateResponse;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SquadRecommendationServiceImplTest {
    @InjectMocks SquadRecommendationServiceImpl squadRecommendationService;

    @Mock private SquadCommandRepository squadCommandRepository;
    @Mock private SquadEmployeeCommandRepository squadEmployeeCommandRepository;
    @Mock private  SquadQueryService squadQueryService;
    @Mock private  SquadCombinationGeneratorImpl squadCombinationGenerator;
    @Mock private  SquadEvaluatorImpl squadEvaluator;
    @Mock private  SquadSelectorImpl squadSelector;
    @Mock private ProjectCommandService projectCommandService;
    @Mock private  SquadDomainService squadDomainService;
    @Mock private  SquadCandidateFilter squadCandidateFilter;

    private final String projectId = "PJT-001";
    private SquadRecommendationRequest request;
    private DeveloperSummary developer;
    private Map<String, List<DeveloperSummary>> candidates;

    @BeforeEach
    void setup() {
        request = SquadRecommendationRequest.builder()
                .projectId(projectId)
                .criteria(RecommendationCriteria.BALANCED)
                .build();

        developer = DeveloperSummary.builder()
                .id("DEV001")
                .name("홍길동")
                .grade("A")
                .avgTechScore(90.0)
                .domainCount(5)
                .monthlyUnitPrice(5000000)
                .productivity(1.2)
                .build();

        candidates = Map.of("백엔드", List.of(developer));
    }

    @Test
    @DisplayName("성공: 추천 스쿼드를 반환한다")
    void recommendSquad_success() {
        Map<String, Integer> required = Map.of("백엔드", 1);
        List<Map<String, List<DeveloperSummary>>> combos = List.of(candidates);

        EvaluatedSquad best = new EvaluatedSquad();
        best.setSquad(candidates);
        best.setEstimatedTotalCost(BigDecimal.valueOf(10000000));
        best.setEstimatedDuration(3);

        given(squadQueryService.findCandidatesByRoles(projectId)).willReturn(new SquadCandidateResponse(candidates));
        given(squadQueryService.findRequiredMemberCountByRoles(projectId)).willReturn(required);
        given(squadCandidateFilter.filterTopNByCriteria(candidates, RecommendationCriteria.BALANCED)).willReturn(candidates);
        given(squadCombinationGenerator.generate(any(), any())).willReturn(combos);
        given(squadEvaluator.evaluateAll(projectId, combos)).willReturn(List.of(best));
        given(squadSelector.selectBest(anyList(), eq(RecommendationCriteria.BALANCED))).willReturn(best);
        given(squadCommandRepository.countByProjectCode(projectId)).willReturn(0L);
        given(squadDomainService.buildRecommendationReason(any(), any())).willReturn("추천 사유");
        given(projectCommandService.findProjectAndJobIdMap(projectId)).willReturn(Map.of("백엔드", 1L));

        SquadRecommendationResponse response = squadRecommendationService.recommendSquad(request);

        assertThat(response).isNotNull();
        assertThat(response.getSquadCode()).isNotBlank();
        then(squadCommandRepository).should(times(1)).save(any(Squad.class));
        then(squadEmployeeCommandRepository).should(times(1)).saveAll(anyList());

    }

    @Test
    @DisplayName("실패: 후보 개발자가 없으면 예외 발생")
    void recommendSquad_noCandidates() {
        // given
        given(squadQueryService.findCandidatesByRoles(projectId)).willReturn(new SquadCandidateResponse(Map.of()));

        // when & then
        assertThatThrownBy(() -> squadRecommendationService.recommendSquad(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.SQUAD_CANDIDATE_FETCH_FAILED.getMessage());

        then(squadCommandRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실패: 조합 생성이 안되면 예외 발생")
    void recommendSquad_noCombinations() {
        // given
        Map<String, Integer> required = Map.of("백엔드", 1);

        given(squadQueryService.findCandidatesByRoles(projectId)).willReturn(new SquadCandidateResponse(candidates));
        given(squadQueryService.findRequiredMemberCountByRoles(projectId)).willReturn(required);
        given(squadCandidateFilter.filterTopNByCriteria(candidates, RecommendationCriteria.BALANCED)).willReturn(candidates);
        given(squadCombinationGenerator.generate(any(), any())).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> squadRecommendationService.recommendSquad(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.SQUAD_GENERATE_CANDIDATE_FAILED.getMessage());
    }
}