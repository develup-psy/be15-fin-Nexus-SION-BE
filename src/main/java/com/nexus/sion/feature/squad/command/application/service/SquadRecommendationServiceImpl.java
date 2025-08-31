package com.nexus.sion.feature.squad.command.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.service.ProjectCommandService;
import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.response.SquadRecommendationResponse;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
import com.nexus.sion.feature.squad.command.domain.service.*;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SquadRecommendationServiceImpl implements SquadRecommendationService {

  private final SquadCommandRepository squadCommandRepository;
  private final SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  private final SquadQueryService squadQueryService;
  private final SquadCombinationGeneratorImpl squadCombinationGenerator;
  private final SquadEvaluatorImpl squadEvaluator;
  private final SquadSelectorImpl squadSelector;
  private final ProjectCommandService projectCommandService;
  private final SquadDomainService squadDomainService;
  private final SquadCandidateFilter squadCandidateFilter;

  @Override
  @Transactional
  public SquadRecommendationResponse recommendSquad(SquadRecommendationRequest request) {
    final String projectId = request.getProjectId();
    final RecommendationCriteria criteria = request.getCriteria();

    Map<String, List<DeveloperSummary>> candidates = loadCandidates(projectId);
    Map<String, Integer> requiredCountByRole = loadRequiredCounts(projectId);

    Map<String, List<DeveloperSummary>> filtered = filterCandidates(candidates, criteria);

    List<Map<String, List<DeveloperSummary>>> combos = generateCombinations(filtered, requiredCountByRole);

    EvaluatedSquad best = evaluateAndSelectBest(projectId, combos, criteria);

    String squadCode = persistRecommendation(projectId, criteria, best);

    return SquadRecommendationResponse.builder().squadCode(squadCode).build();
  }

  private Map<String, List<DeveloperSummary>> loadCandidates(String projectId) {
    Map<String, List<DeveloperSummary>> candidates =
            squadQueryService.findCandidatesByRoles(projectId).candidates();
    boolean empty = candidates.isEmpty() || candidates.values().stream().allMatch(List::isEmpty);
    if (empty) throw new BusinessException(ErrorCode.SQUAD_CANDIDATE_FETCH_FAILED);
    return candidates;
  }

  private Map<String, Integer> loadRequiredCounts(String projectId) {
    return squadQueryService.findRequiredMemberCountByRoles(projectId);
  }

  private Map<String, List<DeveloperSummary>> filterCandidates(
          Map<String, List<DeveloperSummary>> candidates, RecommendationCriteria criteria) {
    return squadCandidateFilter.filterTopNByCriteria(candidates, criteria);
  }

  private List<Map<String, List<DeveloperSummary>>> generateCombinations(
          Map<String, List<DeveloperSummary>> filtered, Map<String, Integer> requiredCountByRole) {
    List<Map<String, List<DeveloperSummary>>> combinations =
            squadCombinationGenerator.generate(filtered, requiredCountByRole);
    if (combinations.isEmpty()) {
      throw new BusinessException(ErrorCode.SQUAD_GENERATE_CANDIDATE_FAILED);
    }
    return combinations;
  }


  private EvaluatedSquad evaluateAndSelectBest(
          String projectId,
          List<Map<String, List<DeveloperSummary>>>  combos,
          RecommendationCriteria criteria) {

    List<EvaluatedSquad> evaluated = squadEvaluator.evaluateAll(projectId, combos);
    return squadSelector.selectBest(evaluated, criteria);
  }

  private String persistRecommendation(
          String projectId, RecommendationCriteria criteria, EvaluatedSquad best) {

    long count = squadCommandRepository.countByProjectCode(projectId);
    String squadCode = SquadCodeGenerator.generate(projectId, count);

    String reason = squadDomainService.buildRecommendationReason(criteria, best);

    Squad squad = Squad.builder()
            .squadCode(squadCode)
            .projectCode(projectId)
            .title("AI 추천 스쿼드 (" + criteria.name() + ")")
            .description("기준: " + criteria.name())
            .isActive(false)
            .estimatedCost(best.getEstimatedTotalCost())
            .estimatedDuration(BigDecimal.valueOf(best.getEstimatedDuration()))
            .originType(OriginType.AI)
            .recommendationReason(reason)
            .build();
    squadCommandRepository.save(squad);

    Map<String, Long> jobIdMap = projectCommandService.findProjectAndJobIdMap(projectId);
    String leaderId = selectLeaderId(best);

    List<SquadEmployee> employees = buildSquadEmployees(best, jobIdMap, squadCode, leaderId);
    squadEmployeeCommandRepository.saveAll(employees);

    return squadCode;
  }

  private String selectLeaderId(EvaluatedSquad best) {
    return best.getSquad().values().stream()
            .flatMap(Collection::stream)
            .max(Comparator.comparingDouble(c -> c.getAvgTechScore() + c.getDomainCount()))
            .map(c -> String.valueOf(c.getId()))
            .orElse(null);
  }

  private List<SquadEmployee> buildSquadEmployees(
          EvaluatedSquad best,
          Map<String, Long> jobIdMap,
          String squadCode,
          String leaderId) {

    LocalDate today = LocalDate.now();

    return best.getSquad().entrySet().stream()
            .flatMap(e -> {
              String jobName = e.getKey();
              Long projectAndJobId = jobIdMap.get(jobName);
              return e.getValue().stream().map(c ->
                      SquadEmployee.builder()
                              .assignedDate(today)
                              .employeeIdentificationNumber(String.valueOf(c.getId()))
                              .projectAndJobId(projectAndJobId)
                              .squadCode(squadCode)
                              .isLeader(String.valueOf(c.getId()).equals(leaderId))
                              .build()
              );
            })
            .toList();
  }
}
