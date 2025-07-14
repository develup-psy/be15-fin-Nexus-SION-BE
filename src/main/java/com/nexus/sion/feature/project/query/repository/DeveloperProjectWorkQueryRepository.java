package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.tables.DeveloperProjectWork.DEVELOPER_PROJECT_WORK;
import static com.example.jooq.generated.tables.DeveloperProjectWorkHistory.DEVELOPER_PROJECT_WORK_HISTORY;
import static com.example.jooq.generated.tables.DeveloperProjectWorkHistoryTechStack.DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK;
import static com.example.jooq.generated.tables.Project.PROJECT;

import java.util.*;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.DeveloperProjectWorkApprovalStatus;
import com.nexus.sion.feature.project.query.dto.request.WorkRequestQueryDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DeveloperProjectWorkQueryRepository {

  private final DSLContext dsl;

  public List<WorkRequestQueryDto> findForAdmin(String status) {
    return findByCondition(null, status);
  }

  public List<WorkRequestQueryDto> findByEmployeeId(String employeeId) {
    return findByCondition(employeeId, null);
  }

  private List<WorkRequestQueryDto> findByCondition(String employeeId, String status) {
    List<Condition> conditions = new ArrayList<>();

    // NOT_REQUESTED 제외하는 조건
    if (employeeId == null) {
      conditions.add(
          DEVELOPER_PROJECT_WORK.APPROVAL_STATUS.ne(
              DeveloperProjectWorkApprovalStatus.NOT_REQUESTED));
    }

    // 특정 사번 필터링
    if (employeeId != null) {
      conditions.add(DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId));
    }

    // 상태 필터링
    if (status != null && !status.isBlank()) {
      conditions.add(
          DEVELOPER_PROJECT_WORK.APPROVAL_STATUS.eq(
              DeveloperProjectWorkApprovalStatus.valueOf(status)));
    }

    var records =
        dsl.select(
                DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID,
                DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER,
                DEVELOPER_PROJECT_WORK.PROJECT_CODE,
                PROJECT.TITLE,
                DEVELOPER_PROJECT_WORK.APPROVAL_STATUS,
                DEVELOPER_PROJECT_WORK.REJECTED_REASON,
                DEVELOPER_PROJECT_WORK.APPROVED_AT,
                DEVELOPER_PROJECT_WORK.CREATED_AT,
                PROJECT.ACTUAL_END_DATE,
                DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID,
                DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_DESCRIPTION,
                DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE,
                DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY,
                DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK.TECH_STACK_NAME)
            .from(DEVELOPER_PROJECT_WORK)
            .leftJoin(PROJECT)
            .on(DEVELOPER_PROJECT_WORK.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
            .leftJoin(DEVELOPER_PROJECT_WORK_HISTORY)
            .on(
                DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID.eq(
                    DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_ID))
            .leftJoin(DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK)
            .on(
                DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID.eq(
                    DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK.DEVELOPER_PROJECT_WORK_HISTORY_ID))
            .where(conditions)
            .orderBy(DEVELOPER_PROJECT_WORK.CREATED_AT.desc())
            .fetch();

    // historyId → tech stack names
    Map<Long, List<String>> historyToStacks =
        records.stream()
            .filter(
                r ->
                    r.get(DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID) != null)
            .collect(
                Collectors.groupingBy(
                    r -> r.get(DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID),
                    Collectors.mapping(
                        r -> r.get(DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK.TECH_STACK_NAME),
                        Collectors.filtering(Objects::nonNull, Collectors.toList()))));

    // workId → histories
    Map<Long, List<WorkRequestQueryDto.WorkRequestHistoryDto>> workToHistories =
        records.stream()
            .filter(
                r ->
                    r.get(DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID) != null)
            .collect(
                Collectors.groupingBy(
                    r -> r.get(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID),
                    Collectors.mapping(
                        r ->
                            new WorkRequestQueryDto.WorkRequestHistoryDto(
                                r.get(
                                    DEVELOPER_PROJECT_WORK_HISTORY
                                        .DEVELOPER_PROJECT_WORK_HISTORY_ID),
                                r.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_DESCRIPTION),
                                historyToStacks.getOrDefault(
                                    r.get(
                                        DEVELOPER_PROJECT_WORK_HISTORY
                                            .DEVELOPER_PROJECT_WORK_HISTORY_ID),
                                    List.of()),
                                Optional.ofNullable(
                                        r.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE))
                                    .map(Enum::name)
                                    .orElse(null),
                                Optional.ofNullable(
                                        r.get(DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY))
                                    .map(Enum::name)
                                    .orElse(null)),
                        Collectors.toList())));

    // 최종 WorkRequestQueryDto 매핑
    return records.stream()
        .collect(
            Collectors.groupingBy(
                r -> r.get(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID),
                LinkedHashMap::new,
                Collectors.toList()))
        .entrySet()
        .stream()
        .map(
            e -> {
              var r = e.getValue().get(0);
              return WorkRequestQueryDto.builder()
                  .workId(r.get(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID))
                  .employeeId(r.get(DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER))
                  .projectCode(r.get(DEVELOPER_PROJECT_WORK.PROJECT_CODE))
                  .projectTitle(r.get(PROJECT.TITLE))
                  .approvalStatus(r.get(DEVELOPER_PROJECT_WORK.APPROVAL_STATUS).name())
                  .rejectedReason(r.get(DEVELOPER_PROJECT_WORK.REJECTED_REASON))
                  .approvedAt(r.get(DEVELOPER_PROJECT_WORK.APPROVED_AT))
                  .createdAt(r.get(DEVELOPER_PROJECT_WORK.CREATED_AT))
                  .histories(workToHistories.getOrDefault(e.getKey(), List.of()))
                  .build();
            })
        .collect(Collectors.toList());
  }

  public long getTotalCountForAdmin(String status) {
    // 총 데이터 수를 계산하는 쿼리
    List<Condition> conditions = new ArrayList<>();
    conditions.add(
        DEVELOPER_PROJECT_WORK.APPROVAL_STATUS.ne(
            DeveloperProjectWorkApprovalStatus.NOT_REQUESTED));
    if (status != null && !status.isBlank()) {
      conditions.add(
          DEVELOPER_PROJECT_WORK.APPROVAL_STATUS.eq(
              DeveloperProjectWorkApprovalStatus.valueOf(status)));
    }
    return dsl.selectCount().from(DEVELOPER_PROJECT_WORK).where(conditions).fetchOne(0, long.class);
  }
}
