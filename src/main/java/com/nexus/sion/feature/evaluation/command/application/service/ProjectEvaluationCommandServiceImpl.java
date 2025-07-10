package com.nexus.sion.feature.evaluation.command.application.service;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.evaluation.command.application.dto.request.ProjectEvaluationRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectEvaluationCommandServiceImpl implements ProjectEvaluationCommandService {

  private final DeveloperTechStackRepository developerTechStackRepository;
  private final SquadCommandRepository squadRepository;

  @Transactional
  public void evaluateProject(ProjectEvaluationRequest request) {
    List<Squad> squads = squadRepository.findByProjectCode(request.getProjectCode());
    if (squads.isEmpty()) {
      throw new BusinessException(ErrorCode.SQUAD_NOT_FOUND, "해당 프로젝트에는 스쿼드가 없습니다.");
    }

    for (ProjectEvaluationRequest.MemberEvaluationDto memberEval : request.getEvaluations()) {
      String employeeId = memberEval.getEmployeeId();
      for (ProjectEvaluationRequest.TechStackScoreDto ts : memberEval.getTechStacks()) {
        String techStackName = ts.getTechStackName();
        int score = ts.getScore();

        DeveloperTechStack existing =
            developerTechStackRepository.findByEmployeeIdentificationNumberAndTechStackName(
                employeeId, techStackName).orElse(null);

        if (existing != null) {
          existing.setTotalScore(existing.getTotalScore() + score);
        } else {
          DeveloperTechStack newStack =
              DeveloperTechStack.builder()
                  .employeeIdentificationNumber(employeeId)
                  .techStackName(techStackName)
                  .totalScore(score)
                  .build();
          developerTechStackRepository.save(newStack);
        }
      }
    }
  }
}
