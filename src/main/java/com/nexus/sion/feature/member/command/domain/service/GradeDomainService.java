package com.nexus.sion.feature.member.command.domain.service;

import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Grade;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.repository.GradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradeDomainService {
  private final GradeRepository gradeRepository;

  public int getMonthlyUnitPrice(String grade) {
    Grade gradeEntity =
        gradeRepository
            .findById(GradeCode.valueOf(grade))
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_GRADE));
    return gradeEntity.getMonthlyUnitPrice();
  }

  public double getProductivityFactor(String grade) {
    Grade gradeEntity =
        gradeRepository
            .findById(GradeCode.valueOf(grade))
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_GRADE));
    return gradeEntity.getProductivity().doubleValue();
  }
}
