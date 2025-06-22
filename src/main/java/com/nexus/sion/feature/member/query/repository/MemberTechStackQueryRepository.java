package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.TechStack.TECH_STACK;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.member.query.dto.response.MemberTechStackResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class MemberTechStackQueryRepository {

  private final DSLContext dsl;

  public List<MemberTechStackResponse> findTechStacksByEmployeeId(String employeeId) {
    return dsl.select(
            TECH_STACK.TECH_STACK_NAME.as("techStackName"),
            DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES)
        .from(DEVELOPER_TECH_STACK)
        .join(MEMBER)
        .on(
            DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
        .join(TECH_STACK)
        .on(DEVELOPER_TECH_STACK.TECH_STACK_NAME.eq(TECH_STACK.TECH_STACK_NAME))
        .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
        .orderBy(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES.desc())
        .fetchInto(MemberTechStackResponse.class);
  }
}
