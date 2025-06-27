package com.nexus.sion.feature.member.query.util;

import com.example.jooq.generated.enums.MemberRole;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import org.jooq.Condition;
import org.springframework.stereotype.Component;
import static com.example.jooq.generated.tables.Member.MEMBER;

@Component
public class MemberConditionBuilder {
    public Condition build(MemberListQuery query) {
        Condition condition = MEMBER.DELETED_AT.isNull()
                .and(MEMBER.ROLE.eq(MemberRole.INSIDER));

        if (query.status() != null) {
            condition = condition.and(MEMBER.STATUS.eq(query.status()));
        }

        if (query.grades() != null && !query.grades().isEmpty()) {
            condition = condition.and(MEMBER.GRADE_CODE.in(query.grades()));
        }


        return condition;
    }
}

