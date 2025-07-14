package com.nexus.sion.feature.member.command.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreDto;
import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreSetRequest;
import com.nexus.sion.feature.member.command.domain.repository.InitialScoreRepository;

@ExtendWith(MockitoExtension.class)
class InitialScoreCommandServiceImplTest {

  @Mock private InitialScoreRepository initialScoreRepository;

  @InjectMocks private InitialScoreCommandServiceImpl initialScoreCommandService;

  @Test
  void setInitialScores_success() {
    // given
    List<InitialScoreDto> scores =
        Arrays.asList(
            InitialScoreDto.builder().minYears(1).maxYears(5).score(50).build(),
            InitialScoreDto.builder().minYears(6).maxYears(10).score(60).build(),
            InitialScoreDto.builder().minYears(11).maxYears(null).score(70).build());

    InitialScoreSetRequest request = InitialScoreSetRequest.builder().initialScores(scores).build();

    // when
    initialScoreCommandService.setInitialScores(request);

    // then
    verify(initialScoreRepository, times(1)).deleteAll();
    verify(initialScoreRepository, times(1)).saveAll(any());
  }

  @Test
  void setInitialScores_firstMinYearsShouldBe1() {
    // given
    List<InitialScoreDto> scores =
        Arrays.asList(
            InitialScoreDto.builder().minYears(2).maxYears(5).score(50).build(),
            InitialScoreDto.builder().minYears(6).maxYears(10).score(60).build(),
            InitialScoreDto.builder().minYears(11).maxYears(null).score(70).build());

    InitialScoreSetRequest request = InitialScoreSetRequest.builder().initialScores(scores).build();

    // when & then
    assertThatThrownBy(() -> initialScoreCommandService.setInitialScores(request))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.FIRST_MIN_YEARS_SHOULD_BE_1);
  }

  @Test
  void setInitialScores_lastMaxYearsShouldBeNull() {
    // given
    List<InitialScoreDto> scores =
        Arrays.asList(
            InitialScoreDto.builder().minYears(1).maxYears(5).score(50).build(),
            InitialScoreDto.builder().minYears(6).maxYears(10).score(60).build(),
            InitialScoreDto.builder()
                .minYears(11)
                .maxYears(15)
                .score(70)
                .build() // 마지막 maxYears가 null이 아님
            );

    InitialScoreSetRequest request = InitialScoreSetRequest.builder().initialScores(scores).build();

    // when & then
    assertThatThrownBy(() -> initialScoreCommandService.setInitialScores(request))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.LAST_MAX_YEARS_SHOULD_BE_NULL);
  }

  @Test
  void setInitialScores_intervalYearsShouldBeContinuous() {
    // given
    List<InitialScoreDto> scores =
        Arrays.asList(
            InitialScoreDto.builder().minYears(1).maxYears(5).score(50).build(),
            InitialScoreDto.builder()
                .minYears(7)
                .maxYears(10)
                .score(60)
                .build(), // 5 + 1 != 7 -> 연속성 오류
            InitialScoreDto.builder().minYears(11).maxYears(null).score(70).build());

    InitialScoreSetRequest request = InitialScoreSetRequest.builder().initialScores(scores).build();

    assertThatThrownBy(() -> initialScoreCommandService.setInitialScores(request))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.INTERVAL_YEARS_SHOULD_BE_CONTINUOUS);
  }
}
