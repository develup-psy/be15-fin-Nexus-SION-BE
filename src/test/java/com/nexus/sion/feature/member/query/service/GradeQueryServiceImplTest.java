package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.query.dto.response.GradeDto;
import com.nexus.sion.feature.member.query.repository.GradeQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeQueryServiceImplTest {

    @Mock
    private GradeQueryRepository gradeQueryRepository;

    @InjectMocks
    private GradeQueryServiceImpl gradeQueryService;


    @Test
    void getGrade_ShouldReturnGradeList() {
        // given
        List<GradeDto> mockGrades = Arrays.asList(
                new GradeDto(GradeCode.S, new BigDecimal("3"), 150000000),
                new GradeDto(GradeCode.A, new BigDecimal("2.5"), 100000000)
        );

        when(gradeQueryRepository.getGrade()).thenReturn(mockGrades);

        // when
        List<GradeDto> result = gradeQueryService.getGrade();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getGradeCode()).isEqualTo(GradeCode.S);
        assertThat(result.get(0).getProductivity()).isEqualTo(new BigDecimal("3"));
        assertThat(result.get(1).getMonthlyUnitPrice()).isEqualTo(100000000);
    }
}
