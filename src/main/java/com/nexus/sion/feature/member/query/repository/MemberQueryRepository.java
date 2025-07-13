package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.Tables.GRADE;
import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.util.List;
import java.util.Optional;

import org.jooq.*;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.MemberRole;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.AdminSearchResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberDetailResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberSquadListResponse;
import com.nexus.sion.feature.member.query.util.TopTechStackSubqueryProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class MemberQueryRepository {

  private final DSLContext dsl;
  private final TopTechStackSubqueryProvider topTechStackSubqueryProvider;

  public long countMembers(Condition condition) {
    Long count = dsl.selectCount().from(MEMBER).where(condition).fetchOneInto(Long.class);

    return count != null ? count : 0L;
  }

  public List<MemberListResponse> findAllMembers(
      MemberListRequest request, Condition baseCondition, SortField<?> sortField) {

    int page = request.getPage();
    int size = request.getSize();
    String keyword = request.getKeyword();

    Condition condition = baseCondition;
    if (keyword != null && !keyword.isBlank()) {
      condition =
          condition.and(
              MEMBER
                  .EMPLOYEE_IDENTIFICATION_NUMBER
                  .containsIgnoreCase(keyword)
                  .or(MEMBER.EMPLOYEE_NAME.containsIgnoreCase(keyword)));
    }

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
            MEMBER.DEPARTMENT_NAME,
            MEMBER.POSITION_NAME,
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
                    record.get(MEMBER.DEPARTMENT_NAME),
                    record.get(MEMBER.POSITION_NAME),
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
            MEMBER.DEPARTMENT_NAME,
            MEMBER.POSITION_NAME,
            MEMBER.PROFILE_IMAGE_URL,
            MEMBER.JOINED_AT,
            topTechStackName,
            MEMBER.CAREER_YEARS)
        .from(MEMBER)
        .leftJoin(topTechStack)
        .on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(topTechStackEmpId).and(rowNumberField.eq(1)))
        .where(
            MEMBER
                .DELETED_AT
                .isNull()
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
                    record.get(MEMBER.DEPARTMENT_NAME),
                    record.get(MEMBER.POSITION_NAME),
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

  public List<AdminSearchResponse> searchAdmins(String keyword, int offset, int limit) {
    String safeKeyword = Optional.ofNullable(keyword).orElse("").trim();
    Condition condition = MEMBER.DELETED_AT.isNull().and(MEMBER.ROLE.eq(MemberRole.ADMIN));

    if (!safeKeyword.isEmpty()) {
      condition = condition.and(MEMBER.EMPLOYEE_NAME.containsIgnoreCase(safeKeyword));
    }

    return dsl.select(
            MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.as("employeeId"),
            MEMBER.EMPLOYEE_NAME.as("name"),
            MEMBER.PROFILE_IMAGE_URL)
        .from(MEMBER)
        .where(condition)
        .orderBy(MEMBER.EMPLOYEE_NAME.asc())
        .offset(offset)
        .limit(limit)
        .fetchInto(AdminSearchResponse.class);
  }

  public int countSearchAdmins(String keyword) {
    String safeKeyword = Optional.ofNullable(keyword).orElse("").trim();
    Condition condition = MEMBER.DELETED_AT.isNull().and(MEMBER.ROLE.eq(MemberRole.ADMIN));

    if (!safeKeyword.isEmpty()) {
      condition = condition.and(MEMBER.EMPLOYEE_NAME.containsIgnoreCase(safeKeyword));
    }

    Integer count = dsl.selectCount().from(MEMBER).where(condition).fetchOneInto(Integer.class);

    return count != null ? count : 0;
  }

  public Optional<MemberDetailResponse> findByEmployeeId(String employeeId) {
    return dsl.select(
            MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.as("employeeId"),
            MEMBER.EMPLOYEE_NAME.as("name"),
            MEMBER.PROFILE_IMAGE_URL.as("profileImageUrl"),
            MEMBER.PHONE_NUMBER.as("phoneNumber"),
            MEMBER.POSITION_NAME.as("position"),
            MEMBER.DEPARTMENT_NAME.as("department"),
            MEMBER.BIRTHDAY,
            MEMBER.JOINED_AT,
            MEMBER.EMAIL,
            MEMBER.CAREER_YEARS,
            MEMBER.SALARY,
            MEMBER.STATUS,
            MEMBER.GRADE_CODE.as("grade"),
            MEMBER.ROLE)
        .from(MEMBER)
        .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
        .fetchOptionalInto(MemberDetailResponse.class);
  }

  public List<MemberSquadListResponse> findAllSquadMembers(
      MemberListQuery query, Condition condition, SortField<?> sortField) {

    TopTechStackSubqueryProvider.TopTechStackSubquery topStack =
        topTechStackSubqueryProvider.getTopTechStackSubquery();

    if (query.techStacks() != null && !query.techStacks().isEmpty()) {
      condition =
          condition.andExists(
              dsl.selectOne()
                  .from(DEVELOPER_TECH_STACK)
                  .where(
                      DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                          MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
                  .and(DEVELOPER_TECH_STACK.TECH_STACK_NAME.in(query.techStacks())));
    }

    return dsl.select(
            MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
            MEMBER.EMPLOYEE_NAME,
            MEMBER.GRADE_CODE,
            MEMBER.STATUS,
            topStack.techStackName(),
            GRADE.MONTHLY_UNIT_PRICE,
            GRADE.PRODUCTIVITY)
        .from(MEMBER)
        .leftJoin(topStack.table())
        .on(
            MEMBER
                .EMPLOYEE_IDENTIFICATION_NUMBER
                .eq(topStack.empId())
                .and(topStack.rowNumberField().eq(1)))
        .join(GRADE) // 등급이 없는 사원의 경우 조회에서 제거
        .on(MEMBER.GRADE_CODE.cast(VARCHAR).eq(GRADE.GRADE_CODE.cast(VARCHAR))) // 등급 기준으로 join
        .where(condition)
        .orderBy(sortField)
        .limit(query.size())
        .offset(query.page() * query.size())
        .fetch(
            record ->
                new MemberSquadListResponse(
                    record.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER),
                    record.get(MEMBER.EMPLOYEE_NAME),
                    record.get(MEMBER.GRADE_CODE) != null
                        ? record.get(MEMBER.GRADE_CODE).name()
                        : null,
                    record.get(MEMBER.STATUS) != null ? record.get(MEMBER.STATUS).name() : null,
                    record.get(topStack.techStackName()),
                    record.get(GRADE.MONTHLY_UNIT_PRICE),
                    record.get(GRADE.PRODUCTIVITY)));
  }
}
