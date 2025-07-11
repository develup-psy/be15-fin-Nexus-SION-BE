package com.nexus.sion.feature.project.command.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
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
  private final NotificationCommandService notificationCommandService;

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
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_NOT_FOUND));

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

    // ===== 알림 전송 로직 추가 =====
    String senderId = work.getEmployeeIdentificationNumber(); // 요청 보낸 개발자

    // 관리자 목록 조회
    List<Member> adminMembers = memberRepository.findAllByRole(MemberRole.ADMIN);
    for (Member admin : adminMembers) {
      notificationCommandService.createAndSendNotification(
          senderId,
          admin.getEmployeeIdentificationNumber(),
          null, // message는 null로 두면 NotificationType이 자동 메시지 생성
          NotificationType.TASK_APPROVAL_REQUEST,
          String.valueOf(workId) // linkedContentId는 작업 ID 사용
          );
    }
  }
}
