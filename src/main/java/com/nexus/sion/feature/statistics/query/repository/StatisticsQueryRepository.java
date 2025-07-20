package com.nexus.sion.feature.statistics.query.repository;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.JobAndTechStack.JOB_AND_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.Project.PROJECT;
import static com.example.jooq.generated.tables.ProjectAndJob.PROJECT_AND_JOB;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;
import static com.example.jooq.generated.tables.TechStack.TECH_STACK;
import static org.jooq.impl.DSL.year;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.MemberGradeCode;
import com.example.jooq.generated.enums.MemberRole;
import com.example.jooq.generated.enums.MemberStatus;
import com.example.jooq.generated.enums.ProjectStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.statistics.query.dto.*;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StatisticsQueryRepository {

  private final DSLContext dsl;

  public List<TechStackCountDto> findStackMemberCount(List<String> techStackNames) {
    return dsl.select(
                    DEVELOPER_TECH_STACK.TECH_STACK_NAME,
                    DSL.countDistinct(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER).as("count"))
            .from(DEVELOPER_TECH_STACK)
            .join(MEMBER)
            .on(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.eq(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
            .where(DEVELOPER_TECH_STACK.TECH_STACK_NAME.in(techStackNames))
            .and(MEMBER.ROLE.ne(MemberRole.ADMIN))
            .groupBy(DEVELOPER_TECH_STACK.TECH_STACK_NAME)
            .fetchInto(TechStackCountDto.class);
  }

  public PageResponse<DeveloperDto> findAllDevelopers(int page, int size) {
    int offset = page * size;

    List<String> memberCodes =
        dsl.select(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER)
            .from(MEMBER)
            .where(MEMBER.DELETED_AT.isNull().and(MEMBER.ROLE.ne(MemberRole.ADMIN)))
            .orderBy(MEMBER.EMPLOYEE_NAME.asc())
            .limit(size)
            .offset(offset)
            .fetchInto(String.class);

    if (memberCodes.isEmpty()) {
      return PageResponse.fromJooq(List.of(), 0L, page, size);
    }

    var records =
        dsl.select(
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                MEMBER.PROFILE_IMAGE_URL,
                MEMBER.EMPLOYEE_NAME,
                MEMBER.POSITION_NAME,
                MEMBER.DEPARTMENT_NAME,
                MEMBER.GRADE_CODE,
                MEMBER.STATUS,
                DEVELOPER_TECH_STACK.TECH_STACK_NAME)
            .from(MEMBER)
            .leftJoin(DEVELOPER_TECH_STACK)
            .on(
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                    DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER))
            .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.in(memberCodes))
            .orderBy(MEMBER.EMPLOYEE_NAME.asc())
            .fetch();

    Map<String, DeveloperDto.DeveloperDtoBuilder> tempMap = new LinkedHashMap<>();
    Map<String, List<String>> stackMap = new HashMap<>();

    for (var record : records) {
      String code = record.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER);

      if (!tempMap.containsKey(code)) {
        List<String> techStacks = new ArrayList<>();
        tempMap.put(
            code,
            DeveloperDto.builder()
                .profileImageUrl(record.get(MEMBER.PROFILE_IMAGE_URL))
                .name(record.get(MEMBER.EMPLOYEE_NAME))
                .position(record.get(MEMBER.POSITION_NAME))
                .department(record.get(MEMBER.DEPARTMENT_NAME))
                .code(code)
                .grade(
                    Optional.ofNullable(record.get(MEMBER.GRADE_CODE)).map(Enum::name).orElse(null))
                .status(Optional.ofNullable(record.get(MEMBER.STATUS)).map(Enum::name).orElse(null))
                .techStacks(techStacks));
        stackMap.put(code, techStacks);
      }

      String stack = record.get(DEVELOPER_TECH_STACK.TECH_STACK_NAME);
      if (stack != null) {
        stackMap.get(code).add(stack);
      }
    }

    List<DeveloperDto> content =
        tempMap.values().stream().map(DeveloperDto.DeveloperDtoBuilder::build).toList();

    long total =
        dsl.selectCount()
            .from(MEMBER)
            .where(MEMBER.DELETED_AT.isNull().and(MEMBER.ROLE.ne(MemberRole.ADMIN)))
            .fetchOne(0, Long.class);

    return PageResponse.fromJooq(content, total, page, size);
  }

  public PageResponse<TechStackCareerDto> findStackAverageCareerPaged(
      List<String> techStackNames, int page, int size, String sort, String direction) {
    int offset = page * size;
    boolean desc = "desc".equalsIgnoreCase(direction);

    Field<?> sortField =
        switch (sort) {
          case "averageCareer" -> DSL.avg(MEMBER.CAREER_YEARS);
          case "count" -> DSL.countDistinct(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER);
          default -> DEVELOPER_TECH_STACK.TECH_STACK_NAME;
        };

    SortField<?> orderBy = desc ? sortField.desc() : sortField.asc();

    // 전체 개수
    long total =
        dsl.select(DEVELOPER_TECH_STACK.TECH_STACK_NAME)
            .from(DEVELOPER_TECH_STACK)
            .where(DEVELOPER_TECH_STACK.TECH_STACK_NAME.in(techStackNames))
            .groupBy(DEVELOPER_TECH_STACK.TECH_STACK_NAME)
            .fetch()
            .size();

    // 데이터 조회
    List<TechStackCareerDto> content =
        dsl.select(
                DEVELOPER_TECH_STACK.TECH_STACK_NAME,
                DSL.avg(MEMBER.CAREER_YEARS).as("averageCareer"),
                DSL.min(MEMBER.CAREER_YEARS).as("minCareer"),
                DSL.max(MEMBER.CAREER_YEARS).as("maxCareer"),
                DSL.countDistinct(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER).as("count"))
            .from(DEVELOPER_TECH_STACK)
            .join(MEMBER)
            .on(
                DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                    MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
            .where(DEVELOPER_TECH_STACK.TECH_STACK_NAME.in(techStackNames))
            .and(MEMBER.DELETED_AT.isNull())
            .and(MEMBER.ROLE.ne(MemberRole.ADMIN))
            .groupBy(DEVELOPER_TECH_STACK.TECH_STACK_NAME)
            .orderBy(orderBy)
            .limit(size)
            .offset(offset)
            .fetchInto(TechStackCareerDto.class);

    return PageResponse.fromJooq(content, total, page, size);
  }

  public PageResponse<TechStackMonthlyUsageDto> findMonthlyPopularTechStacks(
      String period, int page, int size, Integer top) {

    LocalDate now = LocalDate.now();
    LocalDate fromDate =
        switch (period.toLowerCase()) {
          case "1m" -> now.minusMonths(1);
          case "6m" -> now.minusMonths(6);
          case "1y" -> now.minusYears(1);
          case "5y" -> now.minusYears(5);
          default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };

    boolean isDaily = period.equalsIgnoreCase("1m");
    boolean isHalfYear = period.equalsIgnoreCase("5y");
    String datePattern = isDaily ? "%Y-%m-%d" : "%Y-%m";

    Field<String> timeKeyField =
        DSL.field(
            "DATE_FORMAT({0}, {1})", String.class, PROJECT.START_DATE, DSL.inline(datePattern));

    // base CTE
    var baseCte =
        dsl.select(
                JOB_AND_TECH_STACK.TECH_STACK_NAME,
                PROJECT_AND_JOB.JOB_NAME,
                PROJECT.TITLE.as("project_name"),
                PROJECT.START_DATE)
            .from(JOB_AND_TECH_STACK)
            .join(PROJECT_AND_JOB)
            .on(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .join(PROJECT)
            .on(PROJECT_AND_JOB.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
            .where(PROJECT.STATUS.in(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETE))
            .and(PROJECT.START_DATE.ge(fromDate))
            .asTable("base");

    // 실제 사용량 조회
    var usage =
        dsl.select(
                baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                DSL.field(
                        "DATE_FORMAT({0}, {1})",
                        String.class, baseCte.field(PROJECT.START_DATE), DSL.inline(datePattern))
                    .as("time"),
                DSL.count().as("usage_count"))
            .from(baseCte)
            .groupBy(
                baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                DSL.field(
                    "DATE_FORMAT({0}, {1})",
                    String.class, baseCte.field(PROJECT.START_DATE), DSL.inline(datePattern)))
            .fetch();

    // total usage
    Map<String, Integer> totalUsageMap =
        usage.stream()
            .collect(
                Collectors.groupingBy(
                    r -> r.get(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                    Collectors.summingInt(r -> r.get("usage_count", Integer.class))));

    // time key 목록 생성
    List<String> allTimeKeys = new ArrayList<>();
    if (!isDaily) {
      if (isHalfYear) {
        LocalDate cursor = fromDate.withDayOfMonth(1);
        while (!cursor.isAfter(now)) {
          allTimeKeys.add(cursor.format(DateTimeFormatter.ofPattern("yyyy-MM")));
          cursor = cursor.plusMonths(6);
        }
      } else {
        LocalDate cursor = fromDate.withDayOfMonth(1);
        while (!cursor.isAfter(now)) {
          allTimeKeys.add(cursor.format(DateTimeFormatter.ofPattern("yyyy-MM")));
          cursor = cursor.plusMonths(1);
        }
      }
    }

    // monthlyUsageMap
    Map<String, Map<String, Integer>> monthlyUsageMap = new LinkedHashMap<>();
    for (var record : usage) {
      String name = record.get(JOB_AND_TECH_STACK.TECH_STACK_NAME);
      String time = record.get("time", String.class);
      int count = record.get("usage_count", Integer.class);
      monthlyUsageMap.computeIfAbsent(name, k -> new TreeMap<>()).put(time, count);
    }

    // 누락된 key 보완
    if (!isDaily) {
      for (String name : monthlyUsageMap.keySet()) {
        Map<String, Integer> timeMap = monthlyUsageMap.get(name);
        for (String key : allTimeKeys) {
          timeMap.putIfAbsent(key, 0);
        }
      }
    }

    // latest project
    var latestProjects =
        dsl.selectFrom(
                dsl.select(
                        baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                        baseCte.field("project_name", String.class),
                        DSL.rowNumber()
                            .over()
                            .partitionBy(baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME))
                            .orderBy(baseCte.field(PROJECT.START_DATE).desc())
                            .as("rn"))
                    .from(baseCte)
                    .asTable("latest_project"))
            .where(DSL.field("rn", Integer.class).eq(1))
            .fetchMap(JOB_AND_TECH_STACK.TECH_STACK_NAME, DSL.field("project_name", String.class));

    // top job
    var topJobs =
        dsl.selectFrom(
                dsl.select(
                        baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                        baseCte.field(PROJECT_AND_JOB.JOB_NAME),
                        DSL.count().as("job_count"),
                        DSL.rowNumber()
                            .over()
                            .partitionBy(baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME))
                            .orderBy(DSL.count().desc())
                            .as("rn"))
                    .from(baseCte)
                    .groupBy(
                        baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                        baseCte.field(PROJECT_AND_JOB.JOB_NAME))
                    .asTable("top_job"))
            .where(DSL.field("rn", Integer.class).eq(1))
            .fetchMap(JOB_AND_TECH_STACK.TECH_STACK_NAME, PROJECT_AND_JOB.JOB_NAME);

    // 정렬 및 페이징
    List<String> sortedNames =
        totalUsageMap.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .map(Map.Entry::getKey)
            .toList();

    if (top != null) {
      sortedNames = sortedNames.stream().limit(top).toList();
    }

    int total = sortedNames.size();
    int fromIndex = page * size;
    int toIndex = Math.min(fromIndex + size, total);

    if (fromIndex >= toIndex) {
      return PageResponse.fromJooq(List.of(), total, page, size);
    }

    List<TechStackMonthlyUsageDto> content =
        sortedNames.subList(fromIndex, toIndex).stream()
            .map(
                name ->
                    TechStackMonthlyUsageDto.builder()
                        .techStackName(name)
                        .monthlyUsage(monthlyUsageMap.getOrDefault(name, new TreeMap<>()))
                        .totalUsageCount(totalUsageMap.getOrDefault(name, 0))
                        .latestProjectName(latestProjects.get(name))
                        .topJobName(topJobs.get(name))
                        .build())
            .toList();

    return PageResponse.fromJooq(content, total, page, size);
  }

  public List<JobParticipationStatsDto> getJobParticipationStats() {
    var se = SQUAD_EMPLOYEE;
    var pj = PROJECT_AND_JOB;
    var jt = JOB_AND_TECH_STACK;
    var pr = PROJECT;

    // 1. 직무별 유니크 인원 수
    var memberCount =
        dsl.select(
                pj.JOB_NAME.as("job_name"),
                DSL.countDistinct(se.EMPLOYEE_IDENTIFICATION_NUMBER).as("member_count"))
            .from(se)
            .join(pj)
            .on(se.PROJECT_AND_JOB_ID.eq(pj.PROJECT_AND_JOB_ID))
            .join(pr)
            .on(pj.PROJECT_CODE.eq(pr.PROJECT_CODE))
            .where(pr.STATUS.in(DSL.val("IN_PROGRESS"), DSL.val("COMPLETE")))
            .groupBy(pj.JOB_NAME)
            .asTable("member_count");

    // 2. 직무별 기술 스택 사용 횟수
    var techCount =
        dsl.select(
                pj.JOB_NAME.as("job_name"),
                jt.TECH_STACK_NAME.as("tech_stack_name"),
                DSL.count().as("usage_count"))
            .from(pj)
            .join(jt)
            .on(jt.PROJECT_AND_JOB_ID.eq(pj.PROJECT_AND_JOB_ID))
            .join(pr)
            .on(pj.PROJECT_CODE.eq(pr.PROJECT_CODE))
            .where(
                pr.STATUS
                    .in(DSL.val("IN_PROGRESS"), DSL.val("COMPLETE"))
                    .and(jt.TECH_STACK_NAME.isNotNull()))
            .groupBy(pj.JOB_NAME, jt.TECH_STACK_NAME)
            .asTable("tech_count");

    // 3. 기술 스택 랭킹
    var rankedTech =
        dsl.select(
                techCount.field("job_name", String.class),
                techCount.field("tech_stack_name", String.class),
                techCount.field("usage_count", Integer.class),
                DSL.rowNumber()
                    .over()
                    .partitionBy(techCount.field("job_name", String.class))
                    .orderBy(techCount.field("usage_count", Integer.class).desc())
                    .as("rank"))
            .from(techCount)
            .asTable("ranked_tech");

    // 4. 필드 명시
    Field<String> jobNameField = memberCount.field("job_name", String.class);
    Field<Integer> memberCountField = memberCount.field("member_count", Integer.class);
    Field<String> techStackNameField = rankedTech.field("tech_stack_name", String.class);
    Field<Integer> rankField = rankedTech.field("rank", Integer.class);

    // 5. 최종 조합 (DTO 필드명과 일치하는 alias 지정)
    return dsl.select(
            jobNameField.as("job_name"),
            memberCountField,
            DSL.max(DSL.when(rankField.eq(1), techStackNameField)).as("top_tech_stack1"),
            DSL.max(DSL.when(rankField.eq(2), techStackNameField)).as("top_tech_stack2"))
        .from(memberCount)
        .leftJoin(rankedTech)
        .on(rankedTech.field("job_name", String.class).eq(jobNameField))
        .groupBy(jobNameField)
        .fetchInto(JobParticipationStatsDto.class);
  }

  public List<MemberWaitingCountDto> findWaitingCountByGrade() {
    var gradeValues =
        DSL.values(
                DSL.row(MemberGradeCode.S),
                DSL.row(MemberGradeCode.A),
                DSL.row(MemberGradeCode.B),
                DSL.row(MemberGradeCode.C),
                DSL.row(MemberGradeCode.D))
            .as("grades", "grade_code");

    Field<MemberGradeCode> gradeCodeField = gradeValues.field("grade_code", MemberGradeCode.class);

    Table<?> memberSubquery =
        DSL.select(
                MEMBER.GRADE_CODE,
                DSL.count().as("total_count"),
                DSL.sum(DSL.when(MEMBER.STATUS.eq(MemberStatus.AVAILABLE), 1).otherwise(0))
                    .as("waiting_count"))
            .from(MEMBER)
            .where(MEMBER.ROLE.ne(MemberRole.ADMIN))
            .groupBy(MEMBER.GRADE_CODE)
            .asTable("member_stats");

    Field<Integer> waitingCountField =
        DSL.coalesce(memberSubquery.field("waiting_count", Integer.class), 0);
    Field<Integer> totalCountField =
        DSL.coalesce(memberSubquery.field("total_count", Integer.class), 0);

    return dsl.select(gradeCodeField, waitingCountField, totalCountField)
        .from(gradeValues)
        .leftJoin(memberSubquery)
        .on(gradeCodeField.eq(memberSubquery.field(MEMBER.GRADE_CODE)))
        .orderBy(gradeCodeField.asc())
        .fetch(
            record ->
                new MemberWaitingCountDto(
                    record.get(gradeCodeField),
                    record.get(waitingCountField),
                    record.get(totalCountField)));
  }

  // 제안
  public List<GradeSalaryStatsDto> getGradeSalaryStatistics() {
    // 1. 모든 등급을 포함하는 가상 테이블 생성
    var gradeValues =
        DSL.values(
                DSL.row(MemberGradeCode.S),
                DSL.row(MemberGradeCode.A),
                DSL.row(MemberGradeCode.B),
                DSL.row(MemberGradeCode.C),
                DSL.row(MemberGradeCode.D))
            .as("grades", "grade_code");

    Field<MemberGradeCode> gradeCodeField = gradeValues.field("grade_code", MemberGradeCode.class);

    // 2. 등급별 연봉 통계를 계산하는 서브쿼리
    var salaryStatsSubquery =
        dsl.select(
                MEMBER.GRADE_CODE,
                DSL.min(MEMBER.SALARY).as("minSalary"),
                DSL.max(MEMBER.SALARY).as("maxSalary"),
                DSL.avg(MEMBER.SALARY).as("avgSalary"))
            .from(MEMBER)
            .where(
                MEMBER
                    .SALARY
                    .isNotNull()
                    .and(MEMBER.GRADE_CODE.isNotNull())
                    .and(MEMBER.ROLE.ne(MemberRole.ADMIN)))
            .groupBy(MEMBER.GRADE_CODE)
            .asTable("salary_stats");

    // 3. 가상 테이블과 서브쿼리를 LEFT JOIN하여 모든 등급에 대한 결과 보장
    return dsl.select(
            gradeCodeField,
            DSL.coalesce(salaryStatsSubquery.field("minSalary", Long.class), 0L).as("minSalary"),
            DSL.coalesce(salaryStatsSubquery.field("maxSalary", Long.class), 0L).as("maxSalary"),
            DSL.coalesce(salaryStatsSubquery.field("avgSalary", Double.class), 0.0).as("avgSalary"))
        .from(gradeValues)
        .leftJoin(salaryStatsSubquery)
        .on(gradeCodeField.eq(salaryStatsSubquery.field(MEMBER.GRADE_CODE)))
        .orderBy(gradeCodeField.asc())
        .fetch(
            record ->
                new GradeSalaryStatsDto(
                    record.get(gradeCodeField),
                    record.get("minSalary", Long.class),
                    record.get("maxSalary", Long.class),
                    Math.round(record.get("avgSalary", Double.class)))); // 평균값은 반올림
  }

  public List<TechAdoptionTrendDto> findTechAdoptionTrendsByYear(int year) {
    LocalDate fromDate = LocalDate.of(year, 1, 1);
    LocalDate toDate = LocalDate.of(year, 12, 31);

    Field<String> techStackNameField = DSL.field("techStackName", String.class);
    Field<String> projectCodeField = DSL.field("projectCode", String.class);
    Field<Integer> quarterField = DSL.field("quarter", Integer.class);
    Field<Integer> yearField = DSL.val(year).as("year");

    Field<Integer> monthField = DSL.extract(PROJECT.START_DATE, DatePart.MONTH);
    Field<Integer> quarterCalc =
        DSL.when(monthField.le(3), 1)
            .when(monthField.le(6), 2)
            .when(monthField.le(9), 3)
            .otherwise(4);

    // 기술 스택별 프로젝트-분기 조합 (중복 제거)
    Table<?> techProjectQuarter =
        dsl.selectDistinct(
                TECH_STACK.TECH_STACK_NAME.as("techStackName"),
                PROJECT.PROJECT_CODE.as("projectCode"),
                quarterCalc.as("quarter"))
            .from(PROJECT)
            .join(PROJECT_AND_JOB)
            .on(PROJECT.PROJECT_CODE.eq(PROJECT_AND_JOB.PROJECT_CODE))
            .join(JOB_AND_TECH_STACK)
            .on(PROJECT_AND_JOB.PROJECT_AND_JOB_ID.eq(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID))
            .join(TECH_STACK)
            .on(JOB_AND_TECH_STACK.TECH_STACK_NAME.eq(TECH_STACK.TECH_STACK_NAME))
            .where(PROJECT.STATUS.in(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETE))
            .and(PROJECT.START_DATE.between(fromDate, toDate))
            .asTable("tech_project_quarter");

    // 전체 고유 프로젝트 수 (연도 내)
    Table<?> distinctProjectsInYear =
        dsl.selectDistinct(PROJECT.PROJECT_CODE)
            .from(PROJECT)
            .where(PROJECT.STATUS.in(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETE))
            .and(PROJECT.START_DATE.between(fromDate, toDate))
            .asTable("distinct_projects");

    long totalProjectsInYear =
        dsl.selectCount().from(distinctProjectsInYear).fetchOne(0, Long.class);

    // 기술 스택별 분기별 프로젝트 수
    var records =
        dsl.select(
                techStackNameField,
                yearField,
                quarterField,
                DSL.countDistinct(projectCodeField).cast(Long.class).as("projectCount"))
            .from(techProjectQuarter)
            .groupBy(techStackNameField, quarterField)
            .orderBy(quarterField.asc())
            .fetch();

    // 분기별 전체 프로젝트 수
    Map<Integer, Long> totalProjectsPerQuarter =
        dsl.select(
                quarterCalc.as("quarter"),
                DSL.countDistinct(PROJECT.PROJECT_CODE).cast(Long.class).as("total"))
            .from(PROJECT)
            .where(PROJECT.STATUS.in(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETE))
            .and(PROJECT.START_DATE.between(fromDate, toDate))
            .groupBy(quarterCalc)
            .fetchMap(DSL.field("quarter", Integer.class), DSL.field("total", Long.class));

    // 기술 스택별 고유 프로젝트 수 (중복 제거)
    Map<String, Long> techStackProjectCounts =
        dsl.select(
                TECH_STACK.TECH_STACK_NAME,
                DSL.countDistinct(PROJECT.PROJECT_CODE).cast(Long.class).as("projectCount"))
            .from(PROJECT)
            .join(PROJECT_AND_JOB)
            .on(PROJECT.PROJECT_CODE.eq(PROJECT_AND_JOB.PROJECT_CODE))
            .join(JOB_AND_TECH_STACK)
            .on(PROJECT_AND_JOB.PROJECT_AND_JOB_ID.eq(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID))
            .join(TECH_STACK)
            .on(JOB_AND_TECH_STACK.TECH_STACK_NAME.eq(TECH_STACK.TECH_STACK_NAME))
            .where(PROJECT.STATUS.in(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETE))
            .and(PROJECT.START_DATE.between(fromDate, toDate))
            .groupBy(TECH_STACK.TECH_STACK_NAME)
            .fetchMap(TECH_STACK.TECH_STACK_NAME, DSL.field("projectCount", Long.class));

    // DTO 생성
    return records.stream()
        .map(
            record -> {
              String tech = record.get(techStackNameField);
              Integer quarter = record.get(quarterField);
              Long count = record.get("projectCount", Long.class);
              long total = totalProjectsPerQuarter.getOrDefault(quarter, 0L);
              double percentage = (total == 0) ? 0.0 : ((double) count / total) * 100.0;
              double totalPercentage =
                  (totalProjectsInYear == 0)
                      ? 0.0
                      : ((double) techStackProjectCounts.getOrDefault(tech, 0L)
                              / totalProjectsInYear)
                          * 100.0;

              return TechAdoptionTrendDto.builder()
                  .techStackName(tech)
                  .year(year)
                  .quarter(quarter)
                  .projectCount(count)
                  .percentage(percentage)
                  .totalPercentage(totalPercentage)
                  .build();
            })
        .toList();
  }

  public List<Integer> findProjectYears() {
    return dsl.selectDistinct(year(PROJECT.START_DATE))
        .from(PROJECT)
        .orderBy(year(PROJECT.START_DATE).desc())
        .fetchInto(Integer.class);
  }
}
