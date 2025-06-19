package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static org.jooq.impl.DSL.*;

import java.util.List;

import org.jooq.*;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.MemberRole;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class MemberQueryRepository {

  private final DSLContext dsl;

  public long countMembers(Condition condition) {
    Long count = dsl.selectCount().from(MEMBER).where(condition).fetchOneInto(Long.class);

    return count != null ? count : 0L;
  }

  public List<MemberListResponse> findAllMembers(
      MemberListRequest request, Condition condition, SortField<?> sortField) {
    int page = request.getPage();
    int size = request.getSize();

    // 기술스택 점수 높은 1순위 ROW 서브쿼리
    Table<?> topTechStack =
        dsl.select(
                DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER,
                DEVELOPER_TECH_STACK.TECH_STACK_NAME,
                rowNumber()
                    .over()
                    .partitionBy(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER)
                    .orderBy(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES.desc())
                    .as("rn"))
            .from(DEVELOPER_TECH_STACK)
            .asTable("top_tech_stack");

    Field<String> topTechStackEmpId =
        topTechStack.field(
            DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.getName(), String.class);
    Field<String> topTechStackName =
        topTechStack.field(DEVELOPER_TECH_STACK.TECH_STACK_NAME.getName(), String.class);
    Field<Integer> rowNumberField = topTechStack.field("rn", Integer.class);

    return dsl.select(
            MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
            MEMBER.EMPLOYEE_NAME,
            MEMBER.PHONE_NUMBER,
            MEMBER.EMAIL,
            MEMBER.ROLE,
            MEMBER.GRADE_CODE,
            MEMBER.STATUS,
            MEMBER.PROFILE_IMAGE_URL,
            MEMBER.JOINED_AT,
            topTechStackName,
            MEMBER.CAREER_YEARS)
        .from(MEMBER)
        .leftJoin(topTechStack)
        .on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(topTechStackEmpId).and(rowNumberField.eq(1)))
        .where(condition)
        .orderBy(sortField)
        .limit(size)
        .offset(page * size)
        .fetch(
            record ->
                new MemberListResponse(
                    record.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER),
                    record.get(MEMBER.EMPLOYEE_NAME),
                    record.get(MEMBER.PHONE_NUMBER),
                    record.get(MEMBER.EMAIL),
                    record.get(MEMBER.ROLE).name(),
                    record.get(MEMBER.GRADE_CODE) != null
                        ? record.get(MEMBER.GRADE_CODE).name()
                        : null,
                    record.get(MEMBER.STATUS) != null ? record.get(MEMBER.STATUS).name() : null,
                    record.get(MEMBER.PROFILE_IMAGE_URL),
                    record.get(MEMBER.JOINED_AT),
                    record.get(topTechStackName),
                    record.get(MEMBER.CAREER_YEARS)));
  }

  public List<MemberListResponse> searchMembers(String keyword, int offset, int limit) {

    // 기술스택 서브쿼리 정의
    Table<?> topTechStack =
        dsl.select(
                DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER,
                DEVELOPER_TECH_STACK.TECH_STACK_NAME,
                rowNumber()
                    .over()
                    .partitionBy(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER)
                    .orderBy(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES.desc())
                    .as("rn"))
            .from(DEVELOPER_TECH_STACK)
            .asTable("top_tech_stack");

    Field<String> topTechStackEmpId =
        topTechStack.field(
            DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.getName(), String.class);
    Field<String> topTechStackName =
        topTechStack.field(DEVELOPER_TECH_STACK.TECH_STACK_NAME.getName(), String.class);
    Field<Integer> rowNumberField = topTechStack.field("rn", Integer.class);

    // 조회 쿼리
    return dsl.select(
            MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
            MEMBER.EMPLOYEE_NAME,
            MEMBER.PHONE_NUMBER,
            MEMBER.EMAIL,
            MEMBER.ROLE,
            MEMBER.GRADE_CODE,
            MEMBER.STATUS,
            MEMBER.PROFILE_IMAGE_URL,
            MEMBER.JOINED_AT,
            topTechStackName,
            MEMBER.CAREER_YEARS)
        .from(MEMBER)
        .leftJoin(topTechStack)
        .on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(topTechStackEmpId).and(rowNumberField.eq(1)))
        .where(
            MEMBER
                .STATUS
                .eq(MemberStatus.valueOf("AVAILABLE"))
                .and(
                    MEMBER
                        .EMPLOYEE_IDENTIFICATION_NUMBER
                        .containsIgnoreCase(keyword)
                        .or(MEMBER.EMPLOYEE_NAME.containsIgnoreCase(keyword))))
        .orderBy(MEMBER.EMPLOYEE_NAME.asc())
        .offset(offset)
        .limit(limit)
        .fetch(
            record ->
                new MemberListResponse(
                    record.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER),
                    record.get(MEMBER.EMPLOYEE_NAME),
                    record.get(MEMBER.PHONE_NUMBER),
                    record.get(MEMBER.EMAIL),
                    record.get(MEMBER.ROLE).name(),
                    record.get(MEMBER.GRADE_CODE) != null
                        ? record.get(MEMBER.GRADE_CODE).name()
                        : null,
                    record.get(MEMBER.STATUS) != null ? record.get(MEMBER.STATUS).name() : null,
                    record.get(MEMBER.PROFILE_IMAGE_URL),
                    record.get(MEMBER.JOINED_AT),
                    record.get(topTechStackName),
                    record.get(MEMBER.CAREER_YEARS)));
  }

  /** 검색 조건에 해당하는 전체 개발자 수 */
  public int countSearchMembers(String keyword) {
    Integer count =
        dsl.selectCount()
            .from(MEMBER)
            .where(
                MEMBER
                    .ROLE
                    .eq(MemberRole.INSIDER)
                    .and(
                        MEMBER
                            .EMPLOYEE_IDENTIFICATION_NUMBER
                            .containsIgnoreCase(keyword)
                            .or(MEMBER.EMPLOYEE_NAME.containsIgnoreCase(keyword))))
            .fetchOneInto(Integer.class);

    return count != null ? count : 0;
  }
}
