package com.nexus.sion.feature.squad.command.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.service.GradeDomainService;
import com.nexus.sion.feature.project.command.application.service.ProjectCommandService;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.command.application.dto.internal.CandidateSummary;
import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.application.dto.response.SquadRecommendationResponse;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
import com.nexus.sion.feature.squad.command.domain.service.SquadCombinationGeneratorImpl;
import com.nexus.sion.feature.squad.command.domain.service.SquadDomainService;
import com.nexus.sion.feature.squad.command.domain.service.SquadEvaluatorImpl;
import com.nexus.sion.feature.squad.command.domain.service.SquadSelectorImpl;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SquadCommandServiceImpl implements SquadCommandService {

  private final SquadCommandRepository squadCommandRepository;
  private final SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  private final ProjectRepository projectRepository;
  private final SquadCommentRepository squadCommentRepository;
  private final SquadQueryService squadQueryService;
  private final SquadCombinationGeneratorImpl squadCombinationGenerator;
  private final SquadEvaluatorImpl squadEvaluator;
  private final SquadSelectorImpl squadSelector;
  private final GradeDomainService gradeDomainService;
  private final ProjectCommandService projectCommandService;
  private final SquadDomainService squadDomainService;

  @Override
  @Transactional
  public void registerManualSquad(SquadRegisterRequest request) {

    // 1. 프로젝트 정보 가져오기
    Project project =
        projectRepository
            .findByProjectCode(request.getProjectCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // 2. 프로젝트 코드에서 고객사 코드 파싱
    // 예시: "ha_1_1" → "ha_1"
    String[] parts = request.getProjectCode().split("_");
    if (parts.length < 3) {
      throw new BusinessException(ErrorCode.INVALID_SQUAD_PROJECT_CODE_FORMAT);
    }
    String clientCode = parts[0] + "_" + parts[1];

    // 3. 해당 프로젝트의 기존 스쿼드 개수 조회
    long squadCount = squadCommandRepository.countByProjectCode(request.getProjectCode());

    // 4. 스쿼드 코드 생성
    String squadCode = request.getProjectCode() + "_" + (squadCount + 1);

    // 5. 스쿼드 저장
    Squad squad =
        Squad.builder()
            .squadCode(squadCode)
            .projectCode(request.getProjectCode())
            .title(request.getTitle())
            .description(request.getDescription())
            .isActive(false)
            .originType(OriginType.MANUAL)
            .build();

    squadCommandRepository.save(squad);

    // 6. 스쿼드 구성원 저장
    List<SquadEmployee> squadEmployees =
        request.getMembers().stream()
            .map(
                member ->
                    SquadEmployee.builder()
                        .squadCode(squad.getSquadCode())
                        .employeeIdentificationNumber(member.getEmployeeIdentificationNumber())
                        .projectAndJobId(member.getProjectAndJobId())
                        .isLeader(false)
                        .assignedDate(LocalDate.now())
                        .build())
            .toList();

    squadEmployeeCommandRepository.saveAll(squadEmployees);
  }

  @Transactional
  public void updateManualSquad(SquadUpdateRequest request) {
    // 1. 기존 스쿼드 조회
    Squad squad =
        squadCommandRepository
            .findBySquadCode(request.getSquadCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND));

    // 2. 스쿼드 기본 정보 수정
    squad.updateInfo(request.getTitle(), request.getDescription());

    // 3. 기존 스쿼드 구성원 삭제
    squadEmployeeCommandRepository.deleteBySquadCode(squad.getSquadCode());

    // 4. 새로운 스쿼드 구성원 등록
    List<SquadEmployee> squadEmployees =
        request.getMembers().stream()
            .map(
                member ->
                    SquadEmployee.builder()
                        .squadCode(squad.getSquadCode())
                        .employeeIdentificationNumber(member.getEmployeeIdentificationNumber())
                        .projectAndJobId(member.getProjectAndJobId())
                        .isLeader(false)
                        .assignedDate(LocalDate.now())
                        .build())
            .toList();
    squadEmployeeCommandRepository.saveAll(squadEmployees);
  }

  @Transactional
  public void deleteSquad(String squadCode) {
    // 존재 확인
    Squad squad =
        squadCommandRepository
            .findBySquadCode(squadCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND));

    // 연관 데이터 삭제
    squadEmployeeCommandRepository.deleteBySquadCode(squadCode);
    squadCommentRepository.deleteBySquadCode(squadCode);

    // 스쿼드 삭제
    squadCommandRepository.delete(squad);
  }

  @Override
  public SquadRecommendationResponse recommendSquad(SquadRecommendationRequest request) {
    String projectId = request.getProjectId();
    RecommendationCriteria criteria = request.getCriteria();

    Map<String, List<DeveloperSummary>> candidates =
        squadQueryService.findCandidatesByRoles(projectId).candidates();

    Map<String, Integer> requiredCountByRole =
        squadQueryService.findRequiredMemberCountByRoles(projectId);

    List<Map<String, List<DeveloperSummary>>> combinations =
        squadCombinationGenerator.generate(candidates, requiredCountByRole);

    if (combinations.isEmpty()) {
      throw new IllegalStateException("생성 가능한 스쿼드 조합이 없습니다.");
    }

    List<Map<String, List<CandidateSummary>>> transformedCombinations =
        combinations.stream()
            .map(
                squad ->
                    squad.entrySet().stream()
                        .collect(
                            Collectors.toMap(
                                Map.Entry::getKey,
                                entry ->
                                    entry.getValue().stream()
                                        .map(
                                            dev ->
                                                CandidateSummary.builder()
                                                    .memberId(dev.getId())
                                                    .name(dev.getName())
                                                    .jobName(entry.getKey())
                                                    .techStackScore((int) dev.getAvgTechScore())
                                                    .domainRelevance(dev.getDomainCount() / 10.0)
                                                    .costPerMonth(
                                                        gradeDomainService.getMonthlyUnitPrice(
                                                            dev.getGrade()))
                                                    .productivityFactor(
                                                        gradeDomainService.getProductivityFactor(
                                                            dev.getGrade()))
                                                    .build())
                                        .toList())))
            .toList();

    List<EvaluatedSquad> evaluatedSquads = squadEvaluator.evaluateAll(transformedCombinations);

    EvaluatedSquad bestSquad = squadSelector.selectBest(evaluatedSquads, criteria);

    String squadCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    Squad squad =
        Squad.builder()
            .squadCode(squadCode)
            .projectCode(projectId)
            .title("AI 추천 스쿼드 (" + criteria.name() + ")")
            .description("기준: " + criteria.name())
            .isActive(false)
            .estimatedCost(BigDecimal.valueOf(bestSquad.getTotalMonthlyCost()))
            .estimatedDuration(BigDecimal.valueOf(bestSquad.getEstimatedDuration()))
            .originType(OriginType.AI)
            .recommendationReason(squadDomainService.buildRecommendationReason(criteria, bestSquad))
            .build();

    squadCommandRepository.save(squad);

    Map<String, Long> jobIdMap = projectCommandService.findProjectAndJobIdMap(projectId);

    Optional<CandidateSummary> leaderCandidate =
        bestSquad.getSquad().values().stream()
            .flatMap(Collection::stream)
            .max(Comparator.comparingDouble(c -> c.getTechStackScore() + c.getDomainRelevance()));

    String leaderId =
        leaderCandidate.map(c -> String.valueOf(c.getMemberId())).orElse(null); // 혹시 모를 예외 대비

    List<SquadEmployee> squadEmployees =
        bestSquad.getSquad().entrySet().stream()
            .flatMap(
                entry -> {
                  String jobName = entry.getKey();
                  Long projectAndJobId = jobIdMap.get(jobName);
                  return entry.getValue().stream()
                      .map(
                          candidate ->
                              SquadEmployee.builder()
                                  .assignedDate(LocalDate.now())
                                  .employeeIdentificationNumber(
                                      String.valueOf(candidate.getMemberId()))
                                  .projectAndJobId(projectAndJobId)
                                  .squadCode(squadCode)
                                  .isLeader(
                                      String.valueOf(candidate.getMemberId()).equals(leaderId))
                                  .totalSkillScore(candidate.getTechStackScore())
                                  .build());
                })
            .toList();

    squadEmployeeCommandRepository.saveAll(squadEmployees);

    // 응답 객체 생성 및 반환
    return SquadRecommendationResponse.builder()
        .squadCode(squad.getSquadCode())
        .projectCode(squad.getProjectCode())
        .title(squad.getTitle())
        .description(squad.getDescription())
        .estimatedCost(squad.getEstimatedCost())
        .estimatedDuration(squad.getEstimatedDuration())
        .recommendationReason(squad.getRecommendationReason())
        .members(
            bestSquad.getSquad().entrySet().stream()
                .flatMap(
                    entry -> {
                      String jobName = entry.getKey();
                      return entry.getValue().stream()
                          .map(
                              candidate ->
                                  SquadRecommendationResponse.MemberInfo.builder()
                                      .employeeIdentificationNumber(
                                          String.valueOf(candidate.getMemberId()))
                                      .jobName(jobName)
                                      .isLeader(
                                          String.valueOf(candidate.getMemberId()).equals(leaderId))
                                      .totalSkillScore(candidate.getTechStackScore())
                                      .build());
                    })
                .toList())
        .build();
  }
}
