package com.nexus.sion.feature.statistics.query.repository;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.TechStack.TECH_STACK;

import java.util.*;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
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

  public List<String> findAllStackNames() {
    return dsl.select(TECH_STACK.TECH_STACK_NAME).from(TECH_STACK).fetchInto(String.class);
  }

  public PageResponse<DeveloperDto> findAllDevelopers(int page, int size) {
    int offset = (page - 1) * size;

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
}
