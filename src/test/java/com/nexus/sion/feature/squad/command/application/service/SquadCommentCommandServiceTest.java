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
}
