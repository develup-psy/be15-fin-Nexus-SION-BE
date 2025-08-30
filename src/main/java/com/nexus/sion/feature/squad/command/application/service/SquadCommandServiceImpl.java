package com.nexus.sion.feature.squad.command.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.member.command.domain.service.GradeDomainService;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.project.command.application.service.ProjectCommandService;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.command.application.dto.internal.CandidateSummary;
import com.nexus.sion.feature.squad.command.application.dto.internal.EvaluatedSquad;
import com.nexus.sion.feature.squad.command.application.dto.request.Developer;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRecommendationRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.application.dto.response.SquadRecommendationResponse;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.RecommendationCriteria;
import com.nexus.sion.feature.squad.command.domain.service.*;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SquadCommandServiceImpl implements SquadCommandService {

  private final SquadCommandRepository squadCommandRepository;
  private final SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  private final SquadCommentRepository squadCommentRepository;
  private final SquadQueryService squadQueryService;
  private final SquadCombinationGeneratorImpl squadCombinationGenerator;
  private final SquadEvaluatorImpl squadEvaluator;
  private final SquadSelectorImpl squadSelector;
  private final GradeDomainService gradeDomainService;
  private final ProjectCommandService projectCommandService;
  private final SquadDomainService squadDomainService;
  private final SquadValidationService squadValidationService;
  private final NotificationCommandService notificationCommandService;
  private final ProjectRepository projectRepository;
  private final MemberRepository memberRepository;

  @Override
  @Transactional
  public void registerManualSquad(SquadRegisterRequest request) {

    String projectCode = request.getProjectCode();
    List<Developer> developers = request.getDevelopers();

    Project project = squadValidationService.validateAndGetProject(projectCode);
    squadValidationService.validateSquadTitleUniqueForCreate(request.getTitle(), projectCode);
    squadValidationService.validateDevelopersExist(developers);
    squadValidationService.validateJobRequirements(projectCode, developers);
    squadValidationService.validateBudget(project, request.getEstimatedCost());
    squadValidationService.validateDuration(project, request.getEstimatedDuration());

    long count = squadCommandRepository.countByProjectCode(projectCode);
    String squadCode = SquadCodeGenerator.generate(projectCode, count);

    Squad squad =
        Squad.builder()
            .squadCode(squadCode)
            .projectCode(projectCode)
            .title(request.getTitle())
            .description(request.getDescription())
            .isActive(false)
            .originType(OriginType.MANUAL)
            .estimatedCost(request.getEstimatedCost())
            .estimatedDuration(request.getEstimatedDuration())
            .build();

    squadCommandRepository.save(squad);

    List<SquadEmployee> squadEmployees =
        developers.stream()
            .map(
                dev ->
                    SquadEmployee.builder()
                        .squadCode(squadCode)
                        .employeeIdentificationNumber(dev.getEmployeeId())
                        .projectAndJobId(dev.getProjectAndJobId())
                        .isLeader(dev.getIsLeader() != null && dev.getIsLeader())
                        .build())
            .toList();

    squadEmployeeCommandRepository.saveAll(squadEmployees);
  }

  @Transactional
  public void updateManualSquad(SquadUpdateRequest request) {
    // 1. 기존 스쿼드 조회
    String squadCode = request.getSquadCode();
    Squad squad =
        squadCommandRepository
            .findBySquadCode(request.getSquadCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND));

    String projectCode = squad.getProjectCode();
    List<Developer> developers = request.getDevelopers();

    Project project = squadValidationService.validateAndGetProject(projectCode);
    squadValidationService.validateSquadTitleUniqueForUpdate(
        request.getTitle(), projectCode, request.getSquadCode());
    squadValidationService.validateDevelopersExist(developers);
    squadValidationService.validateJobRequirements(projectCode, developers);
    squadValidationService.validateBudget(project, request.getEstimatedCost());
    squadValidationService.validateDuration(project, request.getEstimatedDuration());

    squad.updateInfo(
        request.getTitle(),
        request.getDescription(),
        request.getEstimatedCost(),
        request.getEstimatedDuration());

    squadEmployeeCommandRepository.deleteBySquadCode(squadCode);

    List<SquadEmployee> newEmployees =
        developers.stream()
            .map(
                dev ->
                    SquadEmployee.builder()
                        .squadCode(squadCode)
                        .employeeIdentificationNumber(dev.getEmployeeId())
                        .projectAndJobId(dev.getProjectAndJobId())
                        .isLeader(dev.getIsLeader() != null && dev.getIsLeader())
                        .assignedDate(LocalDate.now())
                        .build())
            .toList();

    squadEmployeeCommandRepository.saveAll(newEmployees);
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

    // 후보 개발자 조회
    Map<String, List<DeveloperSummary>> candidates =
        squadQueryService.findCandidatesByRoles(projectId).candidates();

    if (candidates.isEmpty() || candidates.values().stream().allMatch(List::isEmpty)) {
      throw new BusinessException(ErrorCode.SQUAD_CANDIDATE_FETCH_FAILED);
    }

    // 직무별 필요 인원 수 조회
    Map<String, Integer> requiredCountByRole =
        squadQueryService.findRequiredMemberCountByRoles(projectId);

    // 추천 기준에 따라 Top N 필터링
    Map<String, List<DeveloperSummary>> filteredCandidates =
        filterTopNByCriteria(candidates, criteria);

    // 조합 생성
    List<Map<String, List<DeveloperSummary>>> combinations =
        squadCombinationGenerator.generate(filteredCandidates, requiredCountByRole);

    if (combinations.isEmpty()) {
      throw new BusinessException(ErrorCode.SQUNAD_GENERATE_CANDIDATE_FAILED);
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

    List<EvaluatedSquad> evaluatedSquads =
        squadEvaluator.evaluateAll(projectId, transformedCombinations);

    EvaluatedSquad bestSquad = squadSelector.selectBest(evaluatedSquads, criteria);

    String projectCode = request.getProjectId();
    long count = squadCommandRepository.countByProjectCode(projectCode);
    String squadCode = SquadCodeGenerator.generate(projectCode, count);

    String reason = squadDomainService.buildRecommendationReason(criteria, bestSquad);

    Squad squad =
        Squad.builder()
            .squadCode(squadCode)
            .projectCode(projectId)
            .title("AI 추천 스쿼드 (" + criteria.name() + ")")
            .description("기준: " + criteria.name())
            .isActive(false)
            .estimatedCost(BigDecimal.valueOf(bestSquad.getEstimatedTotalCost()))
            .estimatedDuration(BigDecimal.valueOf(bestSquad.getEstimatedDuration()))
            .originType(OriginType.AI)
            .recommendationReason(reason)
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
    return SquadRecommendationResponse.builder().squadCode(squadCode).build();
  }

  @Override
  @Transactional
  public void confirmSquad(String squadCode) {
    // 1. 스쿼드 조회 및 확정 처리
    Squad squad =
        squadCommandRepository
            .findBySquadCode(squadCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND));
    squad.confirm();

    // 2. 프로젝트 상태 변경 및 예산 반영
    projectCommandService.updateProjectStatus(
        squad.getProjectCode(), Project.ProjectStatus.IN_PROGRESS);
    projectCommandService.updateProjectBudget(squad.getProjectCode(), squad.getEstimatedCost());

    // 3. 프로젝트 조회
    Project project =
        projectRepository
            .findById(squad.getProjectCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // 4. 스쿼드 멤버 목록 조회
    List<SquadEmployee> squadMembers = squadEmployeeCommandRepository.findBySquadCode(squadCode);

    // ✅ 5. 멤버 ID로 Member 정보 한 번에 조회
    List<String> memberIds =
        squadMembers.stream().map(SquadEmployee::getEmployeeIdentificationNumber).toList();

    Map<String, Member> memberMap =
        memberRepository.findAllById(memberIds).stream()
            .collect(Collectors.toMap(Member::getEmployeeIdentificationNumber, m -> m));

    // 6. 알림 발송
    for (SquadEmployee member : squadMembers) {
      String receiverId = member.getEmployeeIdentificationNumber();
      Member memberEntity = memberMap.get(receiverId);
      if (memberEntity == null) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
      }

      String message =
          NotificationType.SQUAD_CONFIRMED.generateMessage(
              memberEntity.getEmployeeName(), project.getTitle());

      notificationCommandService.createAndSendNotification(
          null, receiverId, message, NotificationType.SQUAD_CONFIRMED, squad.getProjectCode());
    }
  }

  private Map<String, List<DeveloperSummary>> filterTopNByCriteria(
      Map<String, List<DeveloperSummary>> candidates, RecommendationCriteria criteria) {
    final double TOP_RATIO = 0.3;

    Comparator<DeveloperSummary> comparator = switch (criteria) {
      case TECH_STACK -> Comparator.comparing(DeveloperSummary::getAvgTechScore);
      case DOMAIN_MATCH -> Comparator.comparing(DeveloperSummary::getDomainCount);
      case COST_OPTIMIZED -> Comparator.comparing(DeveloperSummary::getMonthlyUnitPrice);
      case TIME_OPTIMIZED -> Comparator.comparing(DeveloperSummary::getProductivity);
      case BALANCED -> Comparator.comparing(DeveloperSummary::getWeight);
    };

    return candidates.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                  List<DeveloperSummary> list = entry.getValue();
                  list.sort(comparator);
                  int topN = (int) Math.ceil(list.size() * TOP_RATIO);
                  return list.subList(0, Math.min(topN, list.size()));
                }));
  }
}
