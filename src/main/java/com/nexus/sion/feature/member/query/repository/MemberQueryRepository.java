package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.Tables.*;
import static com.example.jooq.generated.Tables.DEVELOPER_TECH_STACK_HISTORY;
import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.jooq.generated.enums.*;
import com.example.jooq.generated.tables.MemberScoreHistory;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.tables.Domain;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.*;
import com.nexus.sion.feature.member.query.util.TopTechStackSubqueryProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class MemberQueryRepository {

  private final DSLContext dsl;
  private final TopTechStackSubqueryProvider topTechStackSubqueryProvider;

  private static final int PENDING_APPROVAL_LIMIT = 10;

  public List<DashboardSummaryResponse.PendingProject> findPendingProjects() {
    // 하위 쿼리: PENDING 프로젝트 5개만 추출
    var projects =
        dsl.select(
                PROJECT.PROJECT_CODE,
                PROJECT.TITLE,
                PROJECT.DESCRIPTION,
                PROJECT.BUDGET,
                DOMAIN.NAME,
                PROJECT.START_DATE)
            .from(PROJECT)
            .join(DOMAIN)
            .on(PROJECT.DOMAIN_NAME.eq(Domain.DOMAIN.NAME))
            .where(PROJECT.STATUS.eq(ProjectStatus.WAITING)
                    .and(PROJECT.START_DATE.isNotNull())
                    .and(PROJECT.START_DATE.ge(LocalDate.now()))
            ) // enum
            .orderBy(PROJECT.START_DATE.asc()) // 시작일 임박 기준
            .limit(5)
            .fetch();

    // 프로젝트 코드별 직무 역할 수 조회 (project_and_job 기준)
    Map<String, Map<String, Integer>> rolesMap =
        dsl
            .select(
                PROJECT_AND_JOB.PROJECT_CODE,
                PROJECT_AND_JOB.JOB_NAME,
                PROJECT_AND_JOB.REQUIRED_NUMBER)
            .from(PROJECT_AND_JOB)
            .where(
                PROJECT_AND_JOB.PROJECT_CODE.in(
                    projects.stream().map(p -> p.get(PROJECT.PROJECT_CODE)).toList()))
            .fetchGroups(
                r -> r.get(PROJECT_AND_JOB.PROJECT_CODE),
                r ->
                    Map.entry(
                        r.get(PROJECT_AND_JOB.JOB_NAME), r.get(PROJECT_AND_JOB.REQUIRED_NUMBER)))
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e ->
                        e.getValue().stream()
                            .collect(
                                Collectors.toMap(
                                    Map.Entry::getKey, Map.Entry::getValue, Integer::sum))));

    return projects.stream()
        .map(
            p ->
                DashboardSummaryResponse.PendingProject.builder()
                    .projectCode(p.get(PROJECT.PROJECT_CODE))
                    .title(p.get(PROJECT.TITLE))
                    .description(p.get(PROJECT.DESCRIPTION))
                    .budget(p.get(PROJECT.BUDGET))
                    .domainName(p.get(DOMAIN.NAME))
                    .startDate(p.get(PROJECT.START_DATE))
                    .roles(rolesMap.getOrDefault(p.get(PROJECT.PROJECT_CODE), Map.of()))
                    .build())
        .toList();
  }

  public List<DashboardSummaryResponse.PendingApprovalProject> findPendingApprovalProjects() {
    var records = dsl
            .select(
                    DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID,
                    DEVELOPER_PROJECT_WORK.PROJECT_CODE,
                    PROJECT.TITLE,
                    DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER,
                    MEMBER.EMPLOYEE_NAME,
                    DEVELOPER_PROJECT_WORK.CREATED_AT
            )
            .from(DEVELOPER_PROJECT_WORK)
            .join(PROJECT).on(DEVELOPER_PROJECT_WORK.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
            .join(MEMBER).on(DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER.eq(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
            .where(DEVELOPER_PROJECT_WORK.APPROVAL_STATUS.eq(DeveloperProjectWorkApprovalStatus.PENDING))
            .orderBy(DEVELOPER_PROJECT_WORK.CREATED_AT.asc())
            .limit(PENDING_APPROVAL_LIMIT)
            .fetch();

    return records.stream()
            .map(r -> DashboardSummaryResponse.PendingApprovalProject.builder()
                    .id(r.get(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID))
                    .projectCode(r.get(DEVELOPER_PROJECT_WORK.PROJECT_CODE))
                    .projectTitle(r.get(PROJECT.TITLE))
                    .developerId(r.get(DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER))
                    .developerName(r.get(MEMBER.EMPLOYEE_NAME))
                    .createdAt(r.get(DEVELOPER_PROJECT_WORK.CREATED_AT))
                    .build())
            .toList();
  }

  public List<DashboardSummaryResponse.TopDeveloper> fetchTopDevelopers() {

    // 윈도우 함수를 사용하여 각 개발자별 최신 점수 기록을 효율적으로 조회
    Table<?> rankedScores = dsl.select(
                    MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER,
                    MEMBER_SCORE_HISTORY.TOTAL_TECH_STACK_SCORES,
                    MEMBER_SCORE_HISTORY.TOTAL_CERTIFICATE_SCORES,
                    rowNumber().over(
                            partitionBy(MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER)
                                    .orderBy(MEMBER_SCORE_HISTORY.CREATED_AT.desc())
                    ).as("rn")
            )
            .from(MEMBER_SCORE_HISTORY)
            .asTable("ranked_scores");

    var latestScores =
            dsl.select(
                            rankedScores.field(MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER),
                            rankedScores.field(MEMBER_SCORE_HISTORY.TOTAL_TECH_STACK_SCORES),
                            rankedScores.field(MEMBER_SCORE_HISTORY.TOTAL_CERTIFICATE_SCORES))
                    .from(rankedScores)
                    .where(rankedScores.field("rn", Integer.class).eq(1))
                    .asTable("latest_score");

    var topDevelopers = dsl
            .select(
                    MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                    MEMBER.EMPLOYEE_NAME,
                    MEMBER.GRADE_CODE,
                    MEMBER.PROFILE_IMAGE_URL,
                    latestScores.field(MEMBER_SCORE_HISTORY.TOTAL_TECH_STACK_SCORES).add(
                            latestScores.field(MEMBER_SCORE_HISTORY.TOTAL_CERTIFICATE_SCORES)
                    ).as("total_score")
            )
            .from(MEMBER)
            .leftJoin(latestScores)
            .on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(latestScores.field(MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER)))
            .orderBy(DSL.field("total_score").desc().nullsLast())
            .limit(10)
            .fetch();

    List<String> developerIds =
        topDevelopers.stream().map(r -> r.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER)).toList();

    Map<String, List<String>> techStackMap =
        dsl.select(
                DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER,
                DEVELOPER_TECH_STACK.TECH_STACK_NAME)
            .from(DEVELOPER_TECH_STACK)
            .where(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.in(developerIds))
            .fetchGroups(
                r -> r.get(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER),
                r -> r.get(DEVELOPER_TECH_STACK.TECH_STACK_NAME));

    // DTO 변환
    return topDevelopers.stream()
        .map(
            r ->
                DashboardSummaryResponse.TopDeveloper.builder()
                    .id(r.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
                    .name(r.get(MEMBER.EMPLOYEE_NAME))
                    .grade(
                            Optional.ofNullable(r.get(MEMBER.GRADE_CODE))
                                    .map(Enum::name)
                                    .orElse(null)
                    )
                    .totalScores(r.get("total_score", Integer.class))
                    .profileUrl(r.get(MEMBER.PROFILE_IMAGE_URL))
                    .techStacks(
                        techStackMap.getOrDefault(
                            r.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER), List.of()))
                    .build())
        .toList();
  }

  public List<DashboardSummaryResponse.FreelancerSummary> fetchTopFreelancers() {
    return dsl.select(
            MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
            MEMBER.EMPLOYEE_NAME,
            MEMBER.CAREER_YEARS,
            MEMBER.PROFILE_IMAGE_URL,
            MEMBER.GRADE_CODE)
        .from(MEMBER)
        .where(MEMBER.ROLE.eq(MemberRole.OUTSIDER))
        .orderBy(MEMBER.CAREER_YEARS.desc(), MEMBER.CREATED_AT.desc())
        .limit(5)
        .fetch()
        .map(
            r ->
                new DashboardSummaryResponse.FreelancerSummary(
                    r.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER),
                    r.get(MEMBER.EMPLOYEE_NAME),
                    Optional.ofNullable(r.get(MEMBER.CAREER_YEARS)).orElse(0),
                    r.get(MEMBER.GRADE_CODE) != null ? r.get(MEMBER.GRADE_CODE).name() : "미정",
                    r.get(MEMBER.PROFILE_IMAGE_URL)));
  }

  public DashboardSummaryResponse.DeveloperAvailability fetchDeveloperAvailability() {
    // 1. 전체 가용 인원 수
    Integer totalAvailable =
        dsl.selectCount()
            .from(MEMBER)
            .where(MEMBER.STATUS.eq(MemberStatus.AVAILABLE))
            .fetchOne(0, int.class);

    // 2. 등급별 인원 분포
    List<DashboardSummaryResponse.DeveloperAvailability.GradeDistribution> gradeDistribution =
        dsl.select(MEMBER.GRADE_CODE, DSL.count())
            .from(MEMBER)
            .where(MEMBER.STATUS.eq(MemberStatus.AVAILABLE))
            .groupBy(MEMBER.GRADE_CODE)
            .fetch()
            .map(
                r ->
                    new DashboardSummaryResponse.DeveloperAvailability.GradeDistribution(
                        r.get(MEMBER.GRADE_CODE) != null ? r.get(MEMBER.GRADE_CODE).name() : "미정",
                        r.get(DSL.count())));

    // 3. available 상태 개발자들의 기술스택 목록 (중복 제거 - 20개까지)
    List<String> availableStacks =
        dsl.selectDistinct(DEVELOPER_TECH_STACK.TECH_STACK_NAME)
            .from(DEVELOPER_TECH_STACK)
            .join(MEMBER)
            .on(
                DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                    MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
            .where(MEMBER.STATUS.eq(MemberStatus.AVAILABLE))
                .limit(20)
            .fetch(DEVELOPER_TECH_STACK.TECH_STACK_NAME);

    return new DashboardSummaryResponse.DeveloperAvailability(
        totalAvailable != null ? totalAvailable : 0, gradeDistribution, availableStacks);
  }

  public List<DashboardSummaryResponse.TechStackDemand> fetchTopTechStacks() {
    return dsl.select(
            JOB_AND_TECH_STACK.TECH_STACK_NAME,
            DSL.countDistinct(PROJECT_AND_JOB.PROJECT_CODE).as("project_count"))
        .from(JOB_AND_TECH_STACK)
        .join(PROJECT_AND_JOB)
        .on(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
        .groupBy(JOB_AND_TECH_STACK.TECH_STACK_NAME)
        .orderBy(DSL.countDistinct(PROJECT_AND_JOB.PROJECT_CODE).desc())
        .limit(10)
        .fetch()
        .map(
            r ->
                new DashboardSummaryResponse.TechStackDemand(
                    r.get(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                    r.get("project_count", Integer.class)));
  }

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

  public List<ScoreTrendDto> findMonthlyTotalScoreTrend(String employeeId) {
    Field<String> monthField =
        DSL.field(
            "DATE_FORMAT({0}, {1})",
            String.class, MEMBER_SCORE_HISTORY.CREATED_AT, DSL.inline("%Y-%m"));

    return dsl.select(
            monthField.as("month"),
            DSL.inline((String) null).as("techStackName"),
            MEMBER_SCORE_HISTORY
                .TOTAL_TECH_STACK_SCORES
                .add(MEMBER_SCORE_HISTORY.TOTAL_CERTIFICATE_SCORES)
                .as("score"))
        .from(MEMBER_SCORE_HISTORY)
        .where(MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
        .groupBy(monthField)
        .orderBy(monthField)
        .fetchInto(ScoreTrendDto.class);
  }

  public List<ScoreTrendDto> findMonthlyTechStackScoreTrend(String employeeId) {
    Field<String> monthField =
        DSL.field(
            "DATE_FORMAT({0}, {1})",
            String.class, DEVELOPER_TECH_STACK_HISTORY.CREATED_AT, DSL.inline("%Y-%m"));

    return dsl.select(
            monthField.as("month"),
            DEVELOPER_TECH_STACK.TECH_STACK_NAME.as("techStackName"),
            DSL.sum(DEVELOPER_TECH_STACK_HISTORY.ADDED_SCORE).as("score"))
        .from(DEVELOPER_TECH_STACK_HISTORY)
        .join(DEVELOPER_TECH_STACK)
        .on(
            DEVELOPER_TECH_STACK_HISTORY.DEVELOPER_TECH_STACK_ID.eq(
                DEVELOPER_TECH_STACK.DEVELOPER_TECH_STACK_ID))
        .where(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
        .groupBy(monthField, DEVELOPER_TECH_STACK.TECH_STACK_NAME)
        .orderBy(monthField, DEVELOPER_TECH_STACK.TECH_STACK_NAME)
        .fetchInto(ScoreTrendDto.class);
  }

  public Optional<String> findProfileImageUrlById(String employeeId) {
    return dsl.select(MEMBER.PROFILE_IMAGE_URL)
        .from(MEMBER)
        .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
        .fetchOptionalInto(String.class);
  }
}
