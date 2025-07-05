package com.nexus.sion.feature.member.query.util;

import static com.example.jooq.generated.tables.Member.MEMBER;

import java.util.List;

import org.jooq.Condition;
import org.springframework.stereotype.Component;

import com.example.jooq.generated.enums.MemberRole;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;

@Component
public class MemberConditionBuilder {
  public Condition build(MemberListQuery query) {
    Condition condition = MEMBER.DELETED_AT.isNull();

    if (query.memberRoles() != null && !query.memberRoles().isEmpty()) {
      List<MemberRole> roles =
          query.memberRoles().stream().map(String::toUpperCase).map(MemberRole::valueOf).toList();
      condition = condition.and(MEMBER.ROLE.in(roles));
    }

    if (query.status() != null) {
      condition = condition.and(MEMBER.STATUS.eq(query.status()));
    }

    if (query.grades() != null && !query.grades().isEmpty()) {
      condition = condition.and(MEMBER.GRADE_CODE.in(query.grades()));
    }

    if (query.keyword() != null && !query.keyword().isBlank()) {
      condition = condition.and(MEMBER.EMPLOYEE_NAME.containsIgnoreCase(query.keyword()));
    }

    return condition;
  }
}
