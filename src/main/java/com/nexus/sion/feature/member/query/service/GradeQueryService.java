package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.GradeDto;

import java.util.List;

public interface GradeQueryService {
    List<GradeDto> getGrade();
}
