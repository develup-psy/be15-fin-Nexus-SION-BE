package com.nexus.sion.feature.squad.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.squad.command.application.dto.request.Developer;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;
import com.nexus.sion.feature.squad.command.domain.service.SquadCodeGenerator;
import com.nexus.sion.feature.squad.command.domain.service.SquadValidationService;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class SquadManualServiceImpl implements SquadManualService {
    private final SquadValidationService squadValidationService;
    private final SquadCommandRepository squadCommandRepository;
    private final SquadEmployeeCommandRepository squadEmployeeCommandRepository;
    private final SquadCommentRepository squadCommentRepository;

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
}
