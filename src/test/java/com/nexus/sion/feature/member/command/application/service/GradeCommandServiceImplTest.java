package com.nexus.sion.feature.member.command.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.nexus.sion.feature.member.command.application.dto.request.GradeDto;
import com.nexus.sion.feature.member.command.application.dto.request.UnitPriceSetRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Grade;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.repository.GradeRepository;

@ExtendWith(MockitoExtension.class)
class GradeCommandServiceImplTest {

  @Mock private GradeRepository gradeRepository;

  @Mock private ModelMapper modelMapper;

  @InjectMocks private GradeCommandServiceImpl gradeCommandService;

  @Test
  void setGrades_shouldCreateAndUpdateGradesCorrectly() {
    // given
    GradeDto newGradeDto =
        GradeDto.builder()
            .gradeCode(GradeCode.A)
            .productivity(new BigDecimal(3))
            .monthlyUnitPrice(100000)
            .build();

    GradeDto existingGradeDto =
        GradeDto.builder().gradeCode(GradeCode.B).monthlyUnitPrice(110000).build();

    Grade existingGrade =
        Grade.builder()
            .gradeCode(GradeCode.B)
            .productivity(new BigDecimal(2))
            .monthlyUnitPrice(90000)
            .build();

    List<GradeDto> gradeDtos = List.of(newGradeDto, existingGradeDto);
    UnitPriceSetRequest request = UnitPriceSetRequest.builder().grades(gradeDtos).build();

    when(gradeRepository.findByGradeCode(GradeCode.A)).thenReturn(Optional.empty());
    when(gradeRepository.findByGradeCode(GradeCode.B)).thenReturn(Optional.of(existingGrade));
    when(modelMapper.map(newGradeDto, Grade.class))
        .thenReturn(
            Grade.builder()
                .gradeCode(GradeCode.A)
                .productivity(new BigDecimal(3))
                .monthlyUnitPrice(100000)
                .build());

    // when
    gradeCommandService.setGrades(request);

    // then
    // createGrade 호출 확인
    verify(gradeRepository)
        .save(
            argThat(
                grade ->
                    grade.getGradeCode() == GradeCode.A
                        && grade.getProductivity().compareTo(new BigDecimal(3)) == 0
                        && grade.getMonthlyUnitPrice() == 100000));

    // updateGrade 호출 확인
    verify(gradeRepository)
        .save(
            argThat(
                grade ->
                    grade.getGradeCode() == GradeCode.B
                        && grade.getProductivity().compareTo(new BigDecimal(2)) == 0
                        && // updated
                        grade.getMonthlyUnitPrice() == 110000 // updated
                ));
  }
}
