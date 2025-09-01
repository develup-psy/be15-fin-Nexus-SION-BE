package com.nexus.sion.feature.squad.command.domain.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectAndJobRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.command.application.dto.internal.RequiredJobDto;
import com.nexus.sion.feature.squad.command.application.dto.request.Developer;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SquadValidationService {

  private final ProjectAndJobRepository projectAndJobRepository;
  private final ProjectRepository projectRepository;
  private final MemberRepository memberRepository;
  private final SquadCommandRepository squadCommandRepository;

  public Project validateAndGetProject(String projectCode) {
    return projectRepository
        .findByProjectCode(projectCode)
        .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
  }

  public void validateDevelopersExist(List<Developer> developers) {
    for (Developer dev : developers) {
      if (!memberRepository.existsByEmployeeIdentificationNumber(dev.getEmployeeId())) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사번: " + dev.getEmployeeId());
      }
      if (!projectAndJobRepository.existsById(dev.getProjectAndJobId())) {
        throw new BusinessException(ErrorCode.JOB_NOT_FOUND, "직무 ID: " + dev.getProjectAndJobId());
      }
    }
  }

  public void validateSquadTitleUniqueForCreate(String title, String projectCode) {
    boolean exists = squadCommandRepository.existsByTitleAndProjectCode(title, projectCode);
    if (exists) {
      throw new BusinessException(ErrorCode.SQUAD_TITLE_DUPLICATED);
    }
  }

  public void validateSquadTitleUniqueForUpdate(
      String title, String projectCode, String currentSquadCode) {
    boolean exists =
        squadCommandRepository.existsByTitleAndProjectCodeAndSquadCodeNot(
            title, projectCode, currentSquadCode);
    if (exists) {
      throw new BusinessException(ErrorCode.SQUAD_TITLE_DUPLICATED);
    }
  }

  public void validateBudget(Project project, BigDecimal estimatedCost) {
    if (project.getBudget() != null
        && estimatedCost.compareTo(project.getBudget()) > 0) {
      throw new BusinessException(
          ErrorCode.EXCEED_PROJECT_BUDGET,
          "예산 상한: " + project.getBudget() + ", 요청 금액: " + estimatedCost);
    }
  }

  public void validateJobRequirements(String projectCode, List<Developer> developers) {

    // 1. DB에서 요구 직무 ID 및 인원 수 조회
    List<RequiredJobDto> projections =
        projectAndJobRepository.findRequiredJobsByProjectCode(projectCode);

    Map<Long, Long> requiredJobCounts =
        projections.stream()
            .collect(
                Collectors.toMap(
                    RequiredJobDto::getProjectAndJobId, p -> Long.valueOf(p.getRequiredNumber())));

    // 2. 실제 스쿼드 구성원의 직무별 인원수 집계
    Map<Long, Long> actualJobCounts =
        developers.stream()
            .collect(Collectors.groupingBy(Developer::getProjectAndJobId, Collectors.counting()));

    // 3. 필수 직무 누락 여부 확인
    for (Long requiredJobId : requiredJobCounts.keySet()) {
      if (!actualJobCounts.containsKey(requiredJobId)) {
        throw new BusinessException(
            ErrorCode.MISSING_REQUIRED_JOB, "필수 직무 ID: " + requiredJobId + " 이(가) 포함되어야 합니다.");
      }
    }

    // 4. 각 직무별 인원 수 만족 여부 확인
    for (Map.Entry<Long, Long> entry : requiredJobCounts.entrySet()) {
      long required = entry.getValue();
      long actual = actualJobCounts.getOrDefault(entry.getKey(), 0L);

      if (actual < required) {
        throw new BusinessException(
            ErrorCode.INSUFFICIENT_JOB_MEMBER,
            "직무 ID: " + entry.getKey() + " → 필요 인원: " + required + ", 현재 인원: " + actual);
      }
    }
  }

  public void validateDuration(Project project, BigDecimal estimatedDuration) {
    if (project.getStartDate() != null && project.getExpectedEndDate() != null) {
      double maxMonths =
          ChronoUnit.MONTHS.between(
                  project.getStartDate().withDayOfMonth(1),
                  project.getExpectedEndDate().withDayOfMonth(1))
              + 1;

      BigDecimal maxAllowed = BigDecimal.valueOf(maxMonths);
      if (estimatedDuration.compareTo(maxAllowed) > 0) {
        throw new BusinessException(
            ErrorCode.EXCEED_PROJECT_DURATION,
            "최대 기간: " + maxMonths + "개월, 요청 기간: " + estimatedDuration);
      }
    }
  }
}
