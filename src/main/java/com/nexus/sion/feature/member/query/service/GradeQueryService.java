package com.nexus.sion.feature.member.query.service;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.GradeDto;

public interface GradeQueryService {
  List<GradeDto> getGrade();
}
