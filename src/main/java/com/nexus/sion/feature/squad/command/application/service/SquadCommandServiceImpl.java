package com.nexus.sion.feature.squad.command.application.service;

import java.time.LocalDate;
import java.util.List;

import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.domain.aggregate.enums.OriginType;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SquadCommandServiceImpl implements SquadCommandService {

  private final SquadCommandRepository squadCommandRepository;
  private final SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  private final ProjectRepository projectRepository;
  private final SquadCommentRepository squadCommentRepository;

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
    Squad squad = squadCommandRepository.findBySquadCode(squadCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SQUAD_NOT_FOUND));

    // 연관 데이터 삭제
    squadEmployeeCommandRepository.deleteBySquadCode(squadCode);
    squadCommentRepository.deleteBySquadCode(squadCode);

    // 스쿼드 삭제
    squadCommandRepository.delete(squad);
  }
}
