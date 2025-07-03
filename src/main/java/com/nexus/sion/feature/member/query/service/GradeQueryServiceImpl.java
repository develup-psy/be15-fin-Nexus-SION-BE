package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.GradeDto;
import com.nexus.sion.feature.member.query.repository.GradeQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeQueryServiceImpl implements  GradeQueryService{

    private final GradeQueryRepository gradeQueryRepository;

    @Override
    public List<GradeDto> getGrade() {
        return gradeQueryRepository.getGrade();
    }
}
