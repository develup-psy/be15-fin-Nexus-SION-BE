package com.nexus.sion.feature.squad.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.project.command.application.service.ProjectCommandService;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SquadLifeCycleImpl implements SquadLifeCycle{
    private static SquadCommandRepository squadCommandRepository;
    private static ProjectCommandService projectCommandService;
    private static SquadEmployeeCommandRepository squadEmployeeCommandRepository;
    private static NotificationCommandService notificationCommandService;

    @Override
    @Transactional
    public void confirmSquad(String squadCode) {
        Squad squad =
                squadCommandRepository
                        .findBySquadCode(squadCode)
                        .orElseThrow(() -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND));
        squad.confirm();

        String projectCode = squad.getProjectCode();
        projectCommandService.updateProjectStatus(
                projectCode, Project.ProjectStatus.IN_PROGRESS);

        projectCommandService.updateProjectBudget(projectCode, squad.getEstimatedCost());

        squadEmployeeCommandRepository
                .findBySquadCode(squadCode)
                .forEach(
                        member ->
                                notificationCommandService.createAndSendNotification(
                                        null,
                                        member.getEmployeeIdentificationNumber(),
                                        null,
                                        NotificationType.SQUAD_CONFIRMED,
                                        squad.getProjectCode()));
    }
}
