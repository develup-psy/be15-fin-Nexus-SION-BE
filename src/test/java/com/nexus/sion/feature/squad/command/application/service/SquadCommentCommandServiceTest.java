package com.nexus.sion.feature.squad.command.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadCommentRegisterRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadComment;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;

class SquadCommentCommandServiceTest {

  private SquadCommentRepository squadCommentRepository;
  private SquadCommentCommandService squadCommentCommandService;

  @BeforeEach
  void setUp() {
    squadCommentRepository = mock(SquadCommentRepository.class);
    squadCommentCommandService = new SquadCommentCommandService(squadCommentRepository);
  }

  @Test
  @DisplayName("스쿼드 코멘트를 정상적으로 등록한다")
  void registerComment_success() {
    // given
    String squadCode = "ha_1_1_1";
    SquadCommentRegisterRequest request = new SquadCommentRegisterRequest("EMM001", "테스트 코멘트입니다.");

    // when
    squadCommentCommandService.registerComment(squadCode, request);

    // then
    verify(squadCommentRepository, times(1)).save(any(SquadComment.class));
  }

  @Test
  @DisplayName("내용이 null이면 예외가 발생한다")
  void registerComment_fail_whenContentIsNull() {
    // given
    SquadCommentRegisterRequest request = new SquadCommentRegisterRequest("EMM001", null);

    // when & then
    String squadCode = "ha_1_1_1";
    assertThatThrownBy(() -> squadCommentCommandService.registerComment(squadCode, request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.COMMENT_CONTENT_EMPTY.getMessage());

    verify(squadCommentRepository, never()).save(any());
  }

  @Test
  @DisplayName("스쿼드 코멘트를 정상적으로 삭제한다")
  void deleteComment_success() {
    // given
    String squadCode = "ha_1_1_1";
    Long commentId = 1L;
    SquadComment comment =
        SquadComment.builder()
            .id(commentId)
            .squadCode(squadCode)
            .employeeIdentificationNumber("EMM001")
            .content("삭제 테스트")
            .build();

    when(squadCommentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

    // when
    squadCommentCommandService.deleteComment(squadCode, commentId);

    // then
    verify(squadCommentRepository, times(1)).delete(comment);
  }

  @Test
  @DisplayName("존재하지 않는 코멘트 삭제 시 예외 발생")
  void deleteComment_fail_whenCommentNotFound() {
    // given
    Long commentId = 999L;
    String squadCode = "ha_1_1_1";
    when(squadCommentRepository.findById(commentId)).thenReturn(java.util.Optional.empty());

    // when & then
    assertThatThrownBy(() -> squadCommentCommandService.deleteComment(squadCode, commentId))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.COMMENT_NOT_FOUND.getMessage());

    verify(squadCommentRepository, never()).delete(any());
  }

  @Test
  @DisplayName("코멘트가 다른 스쿼드에 속할 경우 삭제 시 예외가 발생한다")
  void deleteComment_fail_whenSquadCodeMismatches() {
    // given
    String requestSquadCode = "ha_1_1_1";
    Long commentId = 1L;
    SquadComment comment = SquadComment.builder()
            .id(commentId)
            .squadCode("another_squad_code")
            .employeeIdentificationNumber("EMM001")
            .content("테스트")
            .build();

    when(squadCommentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

    // when & then
    assertThatThrownBy(() -> squadCommentCommandService.deleteComment(requestSquadCode, commentId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.INVALID_COMMENT_ACCESS.getMessage());

    verify(squadCommentRepository, never()).delete(any(SquadComment.class));
  }
}
