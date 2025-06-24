package com.nexus.sion.feature.squad.command.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadCommentRegisterRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadComment;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SquadCommentCommandService {

  private final SquadCommentRepository squadCommentRepository;

  public void registerComment(String squadCode, SquadCommentRegisterRequest request) {
    // ✅ content가 null이거나 공백이면 예외 발생
    if (request.getContent() == null || request.getContent().trim().isEmpty()) {
      throw new BusinessException(ErrorCode.COMMENT_CONTENT_EMPTY);
    }

    SquadComment comment =
        SquadComment.builder()
            .squadCode(squadCode)
            .employeeIdentificationNumber(request.getEmployeeIdentificationNumber())
            .content(request.getContent())
            .createdAt(LocalDateTime.now())
            .build();

    squadCommentRepository.save(comment);
  }
}
