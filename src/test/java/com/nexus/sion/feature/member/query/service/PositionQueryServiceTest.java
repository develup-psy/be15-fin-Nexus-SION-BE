package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.PositionResponse;
import com.nexus.sion.feature.member.query.repository.PositionQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class PositionQueryServiceTest {

    @Mock
    private PositionQueryRepository positionQueryRepository;

    @InjectMocks
    private PositionQueryService positionQueryService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("직급 목록 조회 - 성공")
    void getPositions_success() {
        // given
        List<PositionResponse> mockResult = List.of(
                new PositionResponse("백엔드 개발자"),
                new PositionResponse("프론트엔드 개발자"),
                new PositionResponse("PM")
        );

        when(positionQueryRepository.findAllPositions()).thenReturn(mockResult);

        // when
        List<PositionResponse> result = positionQueryService.getPositions();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("positionName")
                .containsExactly("백엔드 개발자", "프론트엔드 개발자", "PM");
    }
}