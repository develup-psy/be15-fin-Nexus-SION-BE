package com.nexus.sion.feature.member.query.repository;

import com.nexus.sion.feature.member.query.dto.response.GradeDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.jooq.generated.Tables.GRADE;

@Repository
@RequiredArgsConstructor
public class GradeQueryRepository {

    private final DSLContext dsl;

    public List<GradeDto> getGrade() {
        return dsl.select(GRADE.GRADE_CODE, GRADE.RATIO, GRADE.PRODUCTIVITY, GRADE.MONTHLY_UNIT_PRICE)
                .from(GRADE)
                .fetchInto(GradeDto.class);
    }
}
