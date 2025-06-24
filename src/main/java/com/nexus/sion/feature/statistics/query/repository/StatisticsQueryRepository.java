package com.nexus.sion.feature.statistics.query.repository;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.JobAndTechStack.JOB_AND_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.Project.PROJECT;
import static com.example.jooq.generated.tables.ProjectAndJob.PROJECT_AND_JOB;

import java.time.LocalDate;
import java.util.*;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.ProjectStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.PopularTechStackDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCareerDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;

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

  public PageResponse<PopularTechStackDto> findPopularTechStacks(
      String period, int page, int sizeOrTop) {

    LocalDate now = LocalDate.now();
    LocalDate fromDate =
        switch (period.toLowerCase()) {
          case "1m" -> now.minusMonths(1);
          case "6m" -> now.minusMonths(6);
          case "1y" -> now.minusYears(1);
          case "5y" -> now.minusYears(5);
          default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };

    // 공통 base CTE
    var baseCte =
        dsl.select(
                JOB_AND_TECH_STACK.TECH_STACK_NAME,
                PROJECT_AND_JOB.JOB_NAME,
                PROJECT.NAME.as("project_name"),
                PROJECT.START_DATE)
            .from(JOB_AND_TECH_STACK)
            .join(PROJECT_AND_JOB)
            .on(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .join(PROJECT)
            .on(PROJECT_AND_JOB.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
            .where(PROJECT.STATUS.in(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETE))
            .and(PROJECT.START_DATE.ge(fromDate))
            .asTable("base");

    // usage count CTE
    var usageCte =
        dsl.select(baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME), DSL.count().as("usage_count"))
            .from(baseCte)
            .groupBy(baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME))
            .asTable("usage");

    // latest project name CTE
    var latestProjectSubCte =
        dsl.select(
                baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                baseCte.field("project_name", String.class).as("latest_project_name"),
                DSL.rowNumber()
                    .over()
                    .partitionBy(baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME))
                    .orderBy(baseCte.field(PROJECT.START_DATE).desc())
                    .as("rn"))
            .from(baseCte)
            .asTable("latest_project_sub");

    var latestProjectCte =
        dsl.select(
                latestProjectSubCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                latestProjectSubCte.field("latest_project_name", String.class))
            .from(latestProjectSubCte)
            .where(latestProjectSubCte.field("rn", Integer.class).eq(1))
            .asTable("latest_project");

    // job rank CTE
    var jobRankCte =
        dsl.select(
                baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                baseCte.field(PROJECT_AND_JOB.JOB_NAME),
                DSL.count().as("job_count"))
            .from(baseCte)
            .groupBy(
                baseCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                baseCte.field(PROJECT_AND_JOB.JOB_NAME))
            .asTable("job_rank");

    var topJobCte =
        dsl.selectDistinct(
                jobRankCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                DSL.firstValue(jobRankCte.field(PROJECT_AND_JOB.JOB_NAME))
                    .over()
                    .partitionBy(jobRankCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME))
                    .orderBy(jobRankCte.field("job_count").desc())
                    .as("top_job_name"))
            .from(jobRankCte)
            .asTable("top_job");

    // 최종 결과 쿼리
    var resultQuery =
        dsl.select(
                usageCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME),
                usageCte.field("usage_count", Long.class),
                latestProjectCte.field("latest_project_name", String.class),
                topJobCte.field("top_job_name", String.class))
            .from(usageCte)
            .leftJoin(latestProjectCte)
            .on(
                usageCte
                    .field(JOB_AND_TECH_STACK.TECH_STACK_NAME)
                    .eq(latestProjectCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME)))
            .leftJoin(topJobCte)
            .on(
                usageCte
                    .field(JOB_AND_TECH_STACK.TECH_STACK_NAME)
                    .eq(topJobCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME)))
            .orderBy(
                usageCte.field("usage_count").desc(),
                usageCte.field(JOB_AND_TECH_STACK.TECH_STACK_NAME).asc(),
                latestProjectCte.field("latest_project_name").asc().nullsLast())
            .limit(sizeOrTop)
            .offset(page * sizeOrTop);

    List<PopularTechStackDto> result =
        resultQuery.fetch(
            record ->
                PopularTechStackDto.builder()
                    .techStackName(record.get(JOB_AND_TECH_STACK.TECH_STACK_NAME))
                    .usageCount(
                        Optional.ofNullable(record.get("usage_count", Long.class))
                            .map(Long::intValue) // Long → int로 안전하게 변환
                            .orElse(0)) // null일 경우 기본값 0
                    .latestProjectName(record.get("latest_project_name", String.class))
                    .topJobName(record.get("top_job_name", String.class))
                    .build());

    int totalCount = dsl.fetchCount(usageCte);
    return PageResponse.fromJooq(result, totalCount, page, sizeOrTop);
  }
}
