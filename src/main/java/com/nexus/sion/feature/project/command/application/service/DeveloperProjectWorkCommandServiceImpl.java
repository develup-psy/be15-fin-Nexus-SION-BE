package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.project.command.application.dto.FunctionScore;
import com.nexus.sion.feature.project.command.application.dto.FunctionScoreDTO;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryAddRequestDto;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistory;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistoryTechStack;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkHistoryRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkHistoryTechStackRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperProjectWorkCommandServiceImpl implements DeveloperProjectWorkCommandService {

  private final DeveloperProjectWorkRepository workRepository;
  private final DeveloperProjectWorkHistoryRepository workHistoryRepository;
  private final DeveloperProjectWorkHistoryTechStackRepository workHistoryTechStackRepository;
  private final NotificationCommandService notificationCommandService;
  private final MemberRepository memberRepository;
  private final ProjectEvaluateCommandServiceImpl projectEvaluateCommandService;
  private final ProjectRepository projectRepository;

  @Override
  @Transactional
  public void approve(Long id, String adminId) {
    validateAdmin(adminId);

    DeveloperProjectWork work = workRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_HISTORY_NOT_FOUND));

    work.approve(adminId);

    // ===== 점수 산정 로직 =====
    List<DeveloperProjectWorkHistory> histories =
            workHistoryRepository.findAllByDeveloperProjectWorkId(work.getId());

    List<Long> historyIds = histories.stream()
            .map(DeveloperProjectWorkHistory::getId)
            .toList();

    List<DeveloperProjectWorkHistoryTechStack> techStacks =
            workHistoryTechStackRepository.findAllByDeveloperProjectWorkHistoryIdIn(historyIds);

    Map<Long, List<String>> historyIdToStackNamesMap = techStacks.stream()
            .collect(Collectors.groupingBy(
                    DeveloperProjectWorkHistoryTechStack::getDeveloperProjectWorkHistoryId,
                    Collectors.mapping(
                            DeveloperProjectWorkHistoryTechStack::getTechStackName,
                            Collectors.toList()
                    )
            ));

    List<FunctionScore> functionScores = histories.stream()
            .map(history -> new FunctionScore(
                    history.getFunctionName(),
                    history.getFunctionDescription(),
                    history.getFunctionType().name(),
                    history.getDet(),
                    history.getFtr(),
                    historyIdToStackNamesMap.getOrDefault(history.getId(), List.of())
            ))
            .toList();

    FunctionScoreDTO dto = new FunctionScoreDTO(
            work.getEmployeeIdentificationNumber(),
            work.getProjectCode(),
            functionScores
    );

    projectEvaluateCommandService.evaluateFunctionScores(dto);

    // ===== 승인 알림 전송 =====
    String receiverId = work.getEmployeeIdentificationNumber();
    String receiverName = memberRepository
            .findEmployeeNameByEmployeeIdentificationNumber(receiverId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND));

    String statusMessage = "승인";
    String message = NotificationType.TASK_RESULT.getMessage()
            .replace("{username}", receiverName)
            .replace("{status}", statusMessage);

    notificationCommandService.createAndSendNotification(
            adminId,
            receiverId,
            message,
            NotificationType.TASK_RESULT,
            String.valueOf(id)
    );

    // ===== 모든 멤버 승인 완료 시 관리자에게 알림 =====
    List<DeveloperProjectWork> projectWorks =
            workRepository.findAllByProjectCode(work.getProjectCode());

    boolean allApproved = projectWorks.stream()
            .allMatch(w -> w.getApprovalStatus() == DeveloperProjectWork.ApprovalStatus.APPROVED);

    if (allApproved) {
      String projectName = projectRepository
              .findByProjectCode(work.getProjectCode())
              .map(Project::getTitle)
              .orElse("알 수 없는 프로젝트");
      System.out.println(projectName);

      String notifyMessage = NotificationType.PROJECT_EVALUATION_READY.getMessage()
              .replace("{projectName}", projectName);

      notificationCommandService.createAndSendNotification(
              null, // senderId
              adminId,  // 승인한 관리자에게 알림
              notifyMessage,
              NotificationType.PROJECT_EVALUATION_READY,
              work.getProjectCode()
      );
    }
  }


  @Override
  @Transactional
  public void reject(Long id, String adminId, String reason) {
    validateAdmin(adminId);

    DeveloperProjectWork work =
        workRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_HISTORY_NOT_FOUND));

    work.reject(adminId, reason);

    // ===== 거부 알림 전송 =====
    String receiverId = work.getEmployeeIdentificationNumber();
    String receiverName =
        memberRepository
            .findEmployeeNameByEmployeeIdentificationNumber(receiverId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND));

    String statusMessage = "거부";
    String message =
        NotificationType.TASK_RESULT
            .getMessage()
            .replace("{username}", receiverName)
            .replace("{status}", statusMessage);

    notificationCommandService.createAndSendNotification(
        adminId, receiverId, message, NotificationType.TASK_RESULT, String.valueOf(id));

    // ===== 새로운 이력 생성 및 다시 요청 알림 전송 =====
    DeveloperProjectWork newWork =
        DeveloperProjectWork.builder()
            .employeeIdentificationNumber(receiverId)
            .projectCode(work.getProjectCode())
            .approvalStatus(DeveloperProjectWork.ApprovalStatus.NOT_REQUESTED)
            .build();

    DeveloperProjectWork saved = workRepository.save(newWork);

    String requestAgainMessage =
        NotificationType.TASK_APPROVAL_REQUEST_AGAIN
            .getMessage()
            .replace("{username}", receiverName);

    notificationCommandService.createAndSendNotification(
        adminId,
        receiverId,
        requestAgainMessage,
        NotificationType.TASK_APPROVAL_REQUEST_AGAIN,
        String.valueOf(saved.getId()) // 새로 생성된 workId
        );
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
