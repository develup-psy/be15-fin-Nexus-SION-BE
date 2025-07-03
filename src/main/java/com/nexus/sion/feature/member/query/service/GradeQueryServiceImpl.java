package com.nexus.sion.feature.member.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.query.dto.response.GradeDto;
import com.nexus.sion.feature.member.query.repository.GradeQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradeQueryServiceImpl implements GradeQueryService {

  private final GradeQueryRepository gradeQueryRepository;

  @Override
  public List<GradeDto> getGrade() {
    return gradeQueryRepository.getGrade();
  }
}
