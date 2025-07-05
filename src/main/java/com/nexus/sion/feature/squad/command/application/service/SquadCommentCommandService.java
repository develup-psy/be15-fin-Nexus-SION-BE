package com.nexus.sion.feature.squad.command.application.service;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadCommentRegisterRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadComment;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SquadCommentCommandService {

  private final SquadCommentRepository squadCommentRepository;

  @Transactional
  public void registerComment(String squadCode, SquadCommentRegisterRequest request) {
    if (request.getContent() == null || request.getContent().trim().isEmpty()) {
      throw new BusinessException(ErrorCode.COMMENT_CONTENT_EMPTY);
    }

    SquadComment comment =
        SquadComment.builder()
            .squadCode(squadCode)
            .employeeIdentificationNumber(request.getEmployeeIdentificationNumber())
            .content(request.getContent())
            .build();

    squadCommentRepository.save(comment);
  }

  @org.springframework.transaction.annotation.Transactional
  public void deleteComment(String squadCode, Long commentId) {
    SquadComment comment =
        squadCommentRepository
            .findById(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getSquadCode().equals(squadCode)) {
      throw new BusinessException(ErrorCode.INVALID_COMMENT_ACCESS);
    }

    squadCommentRepository.delete(comment);
  }
}
