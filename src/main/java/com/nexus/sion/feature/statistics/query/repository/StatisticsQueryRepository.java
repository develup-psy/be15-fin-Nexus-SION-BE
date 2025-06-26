package com.nexus.sion.feature.statistics.query.repository;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.JobAndTechStack.JOB_AND_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.Project.PROJECT;
import static com.example.jooq.generated.tables.ProjectAndJob.PROJECT_AND_JOB;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

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
        .where(DEVELOPER_TECH_STACK.TECH_STACK_NAME.in(techStackNames))
        .groupBy(DEVELOPER_TECH_STACK.TECH_STACK_NAME)
        .fetchInto(TechStackCountDto.class);
  }

  public PageResponse<DeveloperDto> findAllDevelopers(int page, int size) {
    int offset = page * size;

    List<String> memberCodes =
        dsl.select(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER)
            .from(MEMBER)
            .where(MEMBER.DELETED_AT.isNull())
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
        dsl.selectCount().from(MEMBER).where(MEMBER.DELETED_AT.isNull()).fetchOne(0, Long.class);

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
                pj.JOB_NAME,
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
        dsl.select(pj.JOB_NAME, jt.TECH_STACK_NAME, DSL.count().as("usage_count"))
            .from(pj)
            .join(jt)
            .on(jt.PROJECT_AND_JOB_ID.eq(pj.PROJECT_AND_JOB_ID))
            .join(pr)
            .on(pj.PROJECT_CODE.eq(pr.PROJECT_CODE))
            .where(pr.STATUS.in(DSL.val("IN_PROGRESS"), DSL.val("COMPLETE")))
            .groupBy(pj.JOB_NAME, jt.TECH_STACK_NAME)
            .asTable("tech_count");

    // 3. 기술 스택 랭킹 매기기
    var rankedTech =
        dsl.select(
                techCount.field(pj.JOB_NAME),
                techCount.field(jt.TECH_STACK_NAME),
                techCount.field("usage_count"),
                DSL.rowNumber()
                    .over()
                    .partitionBy(techCount.field(pj.JOB_NAME))
                    .orderBy(techCount.field("usage_count").desc())
                    .as("rank"))
            .from(techCount)
            .asTable("ranked_tech");

    // 4. 필드 추출 (타입 명시)
    Field<String> jobNameField = memberCount.field(pj.JOB_NAME);
    Field<Integer> memberCountField = memberCount.field("member_count", Integer.class);
    Field<String> techStackNameField = rankedTech.field(jt.TECH_STACK_NAME);
    Field<Integer> rankField = rankedTech.field("rank", Integer.class);

    // 5. 최종 결과 조합
    return dsl.select(
            jobNameField.as("job_name"),
            memberCountField,
            DSL.max(DSL.when(rankField.eq(1), techStackNameField)).as("top1"),
            DSL.max(DSL.when(rankField.eq(2), techStackNameField)).as("top2"))
        .from(memberCount)
        .leftJoin(rankedTech)
        .on(rankedTech.field(pj.JOB_NAME).eq(jobNameField))
        .groupBy(jobNameField)
        .fetchInto(JobParticipationStatsDto.class);
  }
}
