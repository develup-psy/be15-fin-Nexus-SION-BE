package com.nexus.sion.feature.project.command.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryAddRequestDto;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistory;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistoryTechStack;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkHistoryRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkHistoryTechStackRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeveloperProjectWorkServiceImpl implements DeveloperProjectWorkService {

  private final DeveloperProjectWorkRepository workRepository;
  private final DeveloperProjectWorkHistoryRepository workHistoryRepository;
  private final DeveloperProjectWorkHistoryTechStackRepository workHistoryTechStackRepository;

  private final MemberRepository memberRepository;

  @Override
  @Transactional
  public void approve(Long id, String adminId) {
    validateAdmin(adminId);
    DeveloperProjectWork work =
        workRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_HISTORY_NOT_FOUND));
    work.approve(adminId);
  }

  @Override
  @Transactional
  public void reject(Long id, String adminId) {
    validateAdmin(adminId);
    DeveloperProjectWork work =
        workRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_HISTORY_NOT_FOUND));
    work.reject(adminId);
  }

  private void validateAdmin(String adminId) {
    boolean isAdmin =
        memberRepository.existsByEmployeeIdentificationNumberAndRole(adminId, MemberRole.ADMIN);
    if (!isAdmin) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED_APPROVER);
    }
  }

  @Override
  @Transactional
  public void addHistories(Long workId, WorkHistoryAddRequestDto dto) {
    DeveloperProjectWork work =
        workRepository
            .findById(workId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_DEVELOPER_PROJECT_WORK));

    work.setApprovalStatus(DeveloperProjectWork.ApprovalStatus.PENDING);

    for (WorkHistoryAddRequestDto.WorkHistoryItemDto item : dto.getHistories()) {
      DeveloperProjectWorkHistory history =
          DeveloperProjectWorkHistory.builder()
              .developerProjectWorkId(workId)
              .functionName(item.getFunctionName())
              .functionDescription(item.getFunctionDescription())
              .functionType(
                  DeveloperProjectWorkHistory.FunctionType.valueOf(item.getFunctionType()))
              .det(Integer.parseInt(item.getDet()))
              .ftr(Integer.parseInt(item.getFtr()))
              .build();

      workHistoryRepository.save(history);

      List<DeveloperProjectWorkHistoryTechStack> techStacks =
          item.getTechStackNames().stream()
              .map(
                  name ->
                      DeveloperProjectWorkHistoryTechStack.builder()
                          .developerProjectWorkHistoryId(history.getId())
                          .techStackName(name)
                          .build())
              .toList();

      workHistoryTechStackRepository.saveAll(techStacks);
    }
  }
}
