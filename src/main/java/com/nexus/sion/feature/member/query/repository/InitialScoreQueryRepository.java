package com.nexus.sion.feature.member.query.repository;

import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.jooq.generated.Tables.INITIAL_SCORE;

@Repository
@RequiredArgsConstructor
public class InitialScoreQueryRepository {

    private final DSLContext dsl;

    public List<InitialScoreDto> getAllScores() {
        return dsl.select(INITIAL_SCORE.MIN_YEARS, INITIAL_SCORE.MAX_YEARS, INITIAL_SCORE.SCORE)
                .from(INITIAL_SCORE)
                .fetchInto(InitialScoreDto.class);
    }
}
