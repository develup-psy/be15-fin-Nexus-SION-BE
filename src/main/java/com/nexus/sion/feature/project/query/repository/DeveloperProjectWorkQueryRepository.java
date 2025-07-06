package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.tables.DeveloperProjectWork.DEVELOPER_PROJECT_WORK;
import static com.example.jooq.generated.tables.DeveloperProjectWorkHistory.DEVELOPER_PROJECT_WORK_HISTORY;
import static com.nexus.sion.feature.project.query.dto.response.WorkRequestQueryDto.WorkRequestHistoryDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.project.query.dto.response.WorkRequestQueryDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DeveloperProjectWorkQueryRepository {

  private final DSLContext dsl;

  public List<WorkRequestQueryDto> findAll() {
    var result =
        dsl.select(
                DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID,
                DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER,
                DEVELOPER_PROJECT_WORK.PROJECT_CODE,
                DEVELOPER_PROJECT_WORK.APPROVAL_STATUS,
                DEVELOPER_PROJECT_WORK.APPROVED_BY,
                DEVELOPER_PROJECT_WORK.APPROVED_AT,
                DEVELOPER_PROJECT_WORK.CREATED_AT,
                DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID,
                DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_DESCRIPTION,
                DEVELOPER_PROJECT_WORK_HISTORY.TECH_STACK_NAME,
                DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE,
                DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY)
            .from(DEVELOPER_PROJECT_WORK)
            .leftJoin(DEVELOPER_PROJECT_WORK_HISTORY)
            .on(
                DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID.eq(
                    DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_ID))
            .fetch();

    Map<Long, List<WorkRequestHistoryDto>> historyMap =
        result.stream()
            .collect(
                Collectors.groupingBy(
                    r -> r.get(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID),
                    Collectors.mapping(
                        r ->
                            new WorkRequestHistoryDto(
                                r.get(
                                    DEVELOPER_PROJECT_WORK_HISTORY
                                        .DEVELOPER_PROJECT_WORK_HISTORY_ID),
                                r.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_DESCRIPTION),
                                r.get(DEVELOPER_PROJECT_WORK_HISTORY.TECH_STACK_NAME),
                                r.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE) != null
                                    ? r.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE).name()
                                    : null,
                                r.get(DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY) != null
                                    ? r.get(DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY).name()
                                    : null),
                        Collectors.toList())));

    return result.stream()
        .collect(
            Collectors.groupingBy(r -> r.get(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID)))
        .entrySet()
        .stream()
        .map(
            e -> {
              var r = e.getValue().get(0);
              return new WorkRequestQueryDto(
                  r.get(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID),
                  r.get(DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER),
                  r.get(DEVELOPER_PROJECT_WORK.PROJECT_CODE),
                  r.get(DEVELOPER_PROJECT_WORK.APPROVAL_STATUS).name(),
                  r.get(DEVELOPER_PROJECT_WORK.APPROVED_BY),
                  r.get(DEVELOPER_PROJECT_WORK.APPROVED_AT),
                  r.get(DEVELOPER_PROJECT_WORK.CREATED_AT),
                  historyMap.getOrDefault(e.getKey(), List.of()));
            })
        .collect(Collectors.toList());
  }
}
