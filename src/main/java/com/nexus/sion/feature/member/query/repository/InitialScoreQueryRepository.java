package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.Tables.INITIAL_SCORE;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.InitialScoreResponseDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InitialScoreQueryRepository {

  private final DSLContext dsl;

  public List<InitialScoreResponseDto> getAllScores() {
    return dsl.select(INITIAL_SCORE.ID, INITIAL_SCORE.MIN_YEARS, INITIAL_SCORE.MAX_YEARS, INITIAL_SCORE.SCORE)
        .from(INITIAL_SCORE)
        .fetchInto(InitialScoreResponseDto.class);
  }
}
