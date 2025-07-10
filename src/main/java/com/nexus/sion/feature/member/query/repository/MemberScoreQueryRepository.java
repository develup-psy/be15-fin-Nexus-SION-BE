package com.nexus.sion.feature.member.query.repository;

import com.example.jooq.generated.tables.records.MemberScoreHistoryRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.example.jooq.generated.tables.MemberScoreHistory.MEMBER_SCORE_HISTORY;

@Repository
@RequiredArgsConstructor
public class MemberScoreQueryRepository {

    private final DSLContext dsl;

    public MemberScoreHistoryRecord getLatestRecord(String employeeId) {
        return dsl.selectFrom(MEMBER_SCORE_HISTORY)
                .where(MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
                .orderBy(MEMBER_SCORE_HISTORY.CREATED_AT.desc())
                .limit(1)
                .fetchOneInto(MemberScoreHistoryRecord.class);
    }

    public MemberScoreHistoryRecord getPreviousTechScoreChangedRecord(String employeeId, int currentTechScore) {
        return dsl.selectFrom(MEMBER_SCORE_HISTORY)
                .where(MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
                .and(MEMBER_SCORE_HISTORY.TOTAL_TECH_STACK_SCORES.ne(currentTechScore))
                .orderBy(MEMBER_SCORE_HISTORY.CREATED_AT.desc())
                .limit(1)
                .fetchOneInto(MemberScoreHistoryRecord.class);
    }

    public MemberScoreHistoryRecord getPreviousCertificateScoreChangedRecord(String employeeId, int currentCertScore) {
        return dsl.selectFrom(MEMBER_SCORE_HISTORY)
                .where(MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
                .and(MEMBER_SCORE_HISTORY.TOTAL_CERTIFICATE_SCORES.ne(currentCertScore))
                .orderBy(MEMBER_SCORE_HISTORY.CREATED_AT.desc())
                .limit(1)
                .fetchOneInto(MemberScoreHistoryRecord.class);
    }
}