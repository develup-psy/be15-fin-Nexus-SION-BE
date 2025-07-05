package com.nexus.sion.feature.squad.query.repository;

import static com.example.jooq.generated.Tables.MEMBER;
import static com.example.jooq.generated.Tables.SQUAD_COMMENT;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.squad.query.dto.response.SquadCommentResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SquadCommentQueryRepository {

    private final DSLContext dsl;

    public List<SquadCommentResponse> findBySquadCode(String squadCode) {
        return dsl
                .select(
                        SQUAD_COMMENT.COMMENT_ID,
                        MEMBER.EMPLOYEE_NAME,
                        MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                        SQUAD_COMMENT.CONTENT,
                        SQUAD_COMMENT.CREATED_AT
                )
                .from(SQUAD_COMMENT)
                .join(MEMBER)
                .on(SQUAD_COMMENT.EMPLOYEE_IDENTIFICATION_NUMBER.eq(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
                .where(SQUAD_COMMENT.SQUAD_CODE.eq(squadCode))
                .orderBy(SQUAD_COMMENT.CREATED_AT.asc())
                .fetchInto(SquadCommentResponse.class);
    }
}
