package com.nexus.sion.feature.project.command.application.service;

import java.util.List;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryRequestDto;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistory;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkHistoryRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeveloperProjectWorkServiceImpl implements DeveloperProjectWorkService {

  private final DeveloperProjectWorkRepository workRepository;
  private final DeveloperProjectWorkHistoryRepository historyRepository;
  private final MemberRepository memberRepository;

  @Override
  public void approve(Long id, String adminId) {
    validateAdmin(adminId);

    DeveloperProjectWork work =
        workRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORK_HISTORY_NOT_FOUND));
    work.approve(adminId);
  }

  @Override
  public Long requestWork(WorkHistoryRequestDto dto) {
    DeveloperProjectWork work =
        new DeveloperProjectWork(dto.getEmployeeIdentificationNumber(), dto.getProjectCode());
    workRepository.save(work);

    List<DeveloperProjectWorkHistory> histories =
        dto.getHistories().stream()
            .map(item -> new DeveloperProjectWorkHistory(work, item))
            .toList();

    historyRepository.saveAll(histories);
    return work.getId();
  }

  @Override
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
}
