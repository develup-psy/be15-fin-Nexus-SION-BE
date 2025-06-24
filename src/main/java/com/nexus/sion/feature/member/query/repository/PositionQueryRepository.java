package com.nexus.sion.feature.member.query.repository;


import com.example.jooq.generated.tables.Position;
import com.nexus.sion.feature.member.query.dto.response.PositionResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.jooq.generated.tables.Position.POSITION;

@Repository
@RequiredArgsConstructor
public class PositionQueryRepository {

    private final DSLContext dsl;

    public List<PositionResponse> findAllPositions() {
        return dsl.select(POSITION.POSITION_NAME)
                .from(POSITION)
                .orderBy(POSITION.POSITION_NAME.asc())
                .fetchInto(PositionResponse.class);
    }
}
