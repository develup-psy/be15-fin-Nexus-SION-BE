package com.nexus.sion.feature.squad.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexus.sion.feature.squad.query.dto.response.SquadCommentResponse;
import com.nexus.sion.feature.squad.query.repository.SquadCommentQueryRepository;

class SquadCommentQueryServiceImplTest {

  @Mock private SquadCommentQueryRepository squadCommentQueryRepository;

  @InjectMocks private SquadCommentQueryServiceImpl squadCommentQueryService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("스쿼드 댓글 목록 조회 성공")
  void findCommentsBySquadCode_success() {
    // given
    String squadCode = "SQD001";
    SquadCommentResponse comment1 =
        new SquadCommentResponse(1L, "홍길동", "DEV001", "리더 배정이 필요합니다.", LocalDateTime.now());

    given(squadCommentQueryRepository.findBySquadCode(squadCode)).willReturn(List.of(comment1));

    // when
    List<SquadCommentResponse> result = squadCommentQueryService.findCommentsBySquadCode(squadCode);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getContent()).isEqualTo("리더 배정이 필요합니다.");
    verify(squadCommentQueryRepository).findBySquadCode(squadCode);
  }

  @Test
  @DisplayName("댓글이 없는 경우 빈 리스트를 반환한다")
  void findCommentsBySquadCode_returnsEmptyList() {
    // given
    String squadCode = "NO_COMMENTS";
    given(squadCommentQueryRepository.findBySquadCode(squadCode))
        .willReturn(Collections.emptyList());

    // when
    List<SquadCommentResponse> result = squadCommentQueryService.findCommentsBySquadCode(squadCode);

    // then
    assertThat(result).isEmpty();
    verify(squadCommentQueryRepository).findBySquadCode(squadCode);
  }
}
