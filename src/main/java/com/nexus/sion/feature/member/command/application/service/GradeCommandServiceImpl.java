package com.nexus.sion.feature.member.command.application.service;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.command.application.dto.request.GradeDto;
import com.nexus.sion.feature.member.command.application.dto.request.UnitPriceSetRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Grade;
import com.nexus.sion.feature.member.command.repository.GradeCommandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradeCommandServiceImpl implements GradeCommandService {

  private final GradeCommandRepository gradeRepository;
  private final ModelMapper modelMapper;

  @Transactional
  @Override
  public void setGrades(UnitPriceSetRequest request) {
    request
        .getGrades()
        .forEach(
            gradeDto -> {
              Optional<Grade> optionalGrade =
                  gradeRepository.findByGradeCode(gradeDto.getGradeCode());
              // 해당 값이 처음 생긴다면 생성, 변경이라면 변경된 값만 변경
              if (optionalGrade.isPresent()) updateGrade(optionalGrade.get(), gradeDto);
              else createGrade(gradeDto);
            });
  }

  private void createGrade(GradeDto gradeDto) {
    Grade grade = modelMapper.map(gradeDto, Grade.class);
    grade.setRatio();
    gradeRepository.save(grade); // insert
  }

  private void updateGrade(Grade grade, GradeDto gradeDto) {
    if (gradeDto.getProductivity() != null) grade.setProductivity(gradeDto.getProductivity());
    if (gradeDto.getMonthlyUnitPrice() != null)
      grade.setMonthlyUnitPrice(gradeDto.getMonthlyUnitPrice());

    grade.setRatio();
    gradeRepository.save(grade); // update
  }
}
