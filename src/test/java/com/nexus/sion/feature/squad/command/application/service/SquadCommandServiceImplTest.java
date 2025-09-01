package com.nexus.sion.feature.squad.command.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.jooq.generated.enums.GradeGradeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexus.sion.feature.project.command.application.service.ProjectCommandService;
import com.nexus.sion.feature.squad.command.application.dto.internal.CandidateSummary;
import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.response.SquadRecommendationResponse;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
import com.nexus.sion.feature.squad.command.domain.service.*;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import com.nexus.sion.feature.squad.query.dto.response.SquadCandidateResponse;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

class SquadCommandServiceImplTest {

  @InjectMocks private SquadRecommendationServiceImpl squadCommandService;

  @Mock private SquadCommandRepository squadCommandRepository;
  @Mock private SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  @Mock private SquadCommentRepository squadCommentRepository;
  @Mock private SquadValidationService squadValidationService;
  @Mock private SquadManualService squadManualService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }


  @Nested
  @DisplayName("recommendSquad() - AI 스쿼드 추천")
  class RecommendSquadTests {

    @Mock private SquadQueryService squadQueryService;
    @Mock private SquadCombinationGeneratorImpl squadCombinationGenerator;
    @Mock private SquadEvaluatorImpl squadEvaluator;
    @Mock private SquadSelectorImpl squadSelector;
    @Mock private SquadDomainService squadDomainService;
    @Mock private ProjectCommandService projectCommandService;
    @Mock private SquadCommandRepository squadCommandRepository;
    @Mock private SquadEmployeeCommandRepository squadEmployeeCommandRepository;

    @InjectMocks private SquadRecommendationServiceImpl squadCommandService;

    private String projectId = "PJT-001";
    private SquadRecommendationRequest request;
    private DeveloperSummary developer;
    private Map<String, List<DeveloperSummary>> candidates;
    private Map<String, Integer> requiredCount;
    private List<Map<String, List<DeveloperSummary>>> combinations;
    private EvaluatedSquad evaluatedSquad;

    @BeforeEach
    void setup() {
      MockitoAnnotations.openMocks(this);

      request =
          SquadRecommendationRequest.builder()
              .projectId(projectId)
              .criteria(RecommendationCriteria.BALANCED)
              .build();

      developer =
          new DeveloperSummary().builder().weight(0.9).id("DEV001").name("홍길동").grade("A").build();

      candidates = Map.of("백엔드", List.of(developer));
      requiredCount = Map.of("백엔드", 1);
      combinations = List.of(candidates);

      DeveloperSummary developerSummary =
          DeveloperSummary.builder()
              .id("DEV001")
              .name("홍길동")
              .avgTechScore(90)
              .domainCount(3)
              .monthlyUnitPrice(5000000)
              .productivity(1.1)
              .grade("B")
              .build();

      Map<String, List<DeveloperSummary>> squad = Map.of("백엔드", List.of(developerSummary));

      evaluatedSquad = new EvaluatedSquad();
      evaluatedSquad.setSquad(squad);
      evaluatedSquad.setEstimatedTotalCost(BigDecimal.valueOf(15000000));
      evaluatedSquad.setEstimatedDuration(3);

      given(squadQueryService.findCandidatesByRoles(projectId))
          .willReturn(new SquadCandidateResponse(candidates));
      given(squadQueryService.findRequiredMemberCountByRoles(projectId)).willReturn(requiredCount);
      given(squadCombinationGenerator.generate(any(), any())).willReturn(combinations);
      given(squadEvaluator.evaluateAll(eq(projectId), any())).willReturn(List.of(evaluatedSquad));
      given(squadSelector.selectBest(anyList(), eq(RecommendationCriteria.BALANCED)))
          .willReturn(evaluatedSquad);
      given(projectCommandService.findProjectAndJobIdMap(projectId))
          .willReturn(Map.of("백엔드", 100L));
      given(squadDomainService.buildRecommendationReason(any(), any())).willReturn("기준: BALANCED");
      given(squadCommandRepository.countByProjectCode(projectId)).willReturn(2L);
    }

    @Test
    @DisplayName("성공: 추천 스쿼드를 반환한다")
    void recommendSquad_success() {
      // when
      SquadRecommendationResponse response = squadCommandService.recommendSquad(request);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getSquadCode()).isNotBlank();
      then(squadCommandRepository).should(times(1)).save(any(Squad.class));
      then(squadEmployeeCommandRepository).should(times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("경계값: 조합이 없으면 예외 발생")
    void recommendSquad_noCombination_throwsException() {
      // given
      given(squadCombinationGenerator.generate(any(), any())).willReturn(List.of());

      // when & then
      assertThatThrownBy(() -> squadCommandService.recommendSquad(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("생성 가능한 스쿼드 조합이 없습니다.");
    }

    @Test
    @DisplayName("예외: 후보 조회 시 예외 발생 시 추천 실패")
    void recommendSquad_candidateFetchFails() {
      // given
      given(squadQueryService.findCandidatesByRoles(projectId))
          .willThrow(new RuntimeException("DB 실패"));

      // when & then
      assertThatThrownBy(() -> squadCommandService.recommendSquad(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("DB 실패");
      then(squadCommandRepository).shouldHaveNoInteractions();
    }
  }
}
