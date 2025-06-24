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
