package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.Tables.DEVELOPER_PROJECT_WORK_HISTORY;
import static com.example.jooq.generated.tables.DeveloperProjectWork.DEVELOPER_PROJECT_WORK;
import static com.example.jooq.generated.tables.DeveloperProjectWorkHistoryTechStack.DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK;
import static com.example.jooq.generated.tables.JobAndTechStack.JOB_AND_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.Project.PROJECT;
import static com.example.jooq.generated.tables.ProjectAndJob.PROJECT_AND_JOB;
import static com.example.jooq.generated.tables.Squad.SQUAD;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;
import static com.example.jooq.generated.tables.TechStack.TECH_STACK;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.DeveloperProjectWorkHistoryComplexity;
import com.example.jooq.generated.enums.DeveloperProjectWorkHistoryFunctionType;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.*;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {

  private final DSLContext dsl;

  public PageResponse<ProjectListResponse> findProjects(ProjectListRequest request) {
    // 키워드 조건 (내부 OR)
    Condition keywordCondition = DSL.noCondition();
    if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
      String keyword = "%" + request.getKeyword() + "%";
      keywordCondition =
          PROJECT
              .TITLE
              .likeIgnoreCase(keyword)
              .or(PROJECT.DOMAIN_NAME.likeIgnoreCase(keyword))
              .or(PROJECT.DESCRIPTION.likeIgnoreCase(keyword));
    }

    // 필터 조건 (AND)
    Condition filterCondition = PROJECT.DELETED_AT.isNull();

    if (request.getMaxBudget() != null) {
      filterCondition = filterCondition.and(PROJECT.BUDGET.le(request.getMaxBudget()));
    }

    if (request.getMaxNumberOfMembers() != null) {
      filterCondition =
          filterCondition.and(PROJECT.NUMBER_OF_MEMBERS.le(request.getMaxNumberOfMembers()));
    }

    if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
      filterCondition = filterCondition.and(PROJECT.STATUS.in(request.getStatuses()));
    }

    if (request.getMaxPeriodInMonth() != null) {
      Field<LocalDate> endDate = DSL.coalesce(PROJECT.ACTUAL_END_DATE, PROJECT.EXPECTED_END_DATE);
      Field<Integer> months =
          DSL.field("timestampdiff(month, {0}, {1})", Integer.class, PROJECT.START_DATE, endDate);
      filterCondition = filterCondition.and(months.le(request.getMaxPeriodInMonth()));
    }

    // 최종 WHERE 조건: keyword AND filter
    Condition finalCondition = keywordCondition.and(filterCondition);

    // 전체 개수
    long totalCount = dsl.selectCount().from(PROJECT).where(finalCondition).fetchOne(0, long.class);

    // 데이터 조회
    List<ProjectListResponse> content =
        dsl
            .selectFrom(PROJECT)
            .where(finalCondition)
            .orderBy(PROJECT.CREATED_AT.desc())
            .limit(request.getSize())
            .offset(request.getPage() * request.getSize())
            .fetch()
            .stream()
            .map(
                record -> {
                  LocalDate start = record.get(PROJECT.START_DATE);
                  LocalDate end =
                      record.get(PROJECT.ACTUAL_END_DATE) != null
                          ? record.get(PROJECT.ACTUAL_END_DATE)
                          : record.get(PROJECT.EXPECTED_END_DATE);

                  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                  String formattedStart = formatter.format(start);
                  String formattedEnd = formatter.format(end);
                  int months = (int) ChronoUnit.MONTHS.between(start, end);

                  return new ProjectListResponse(
                      record.get(PROJECT.PROJECT_CODE),
                      record.get(PROJECT.TITLE),
                      record.get(PROJECT.DESCRIPTION),
                      formattedStart,
                      formattedEnd,
                      months,
                      String.valueOf(record.get(PROJECT.STATUS)),
                      record.get(PROJECT.DOMAIN_NAME),
                      record.get(PROJECT.NUMBER_OF_MEMBERS));
                })
            .collect(Collectors.toList());

    return PageResponse.fromJooq(content, totalCount, request.getPage(), request.getSize());
  }

  public ProjectDetailResponse getProjectDetail(String projectCode) {
    // 1. 프로젝트 기본 정보
    Record project = dsl.selectFrom(PROJECT).where(PROJECT.PROJECT_CODE.eq(projectCode)).fetchOne();

    if (project == null) {
      throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
    }

    // 2. 기간 계산
    LocalDate start = project.get(PROJECT.START_DATE);
    LocalDate end =
        project.get(PROJECT.ACTUAL_END_DATE) != null
            ? project.get(PROJECT.ACTUAL_END_DATE)
            : project.get(PROJECT.EXPECTED_END_DATE);
    String duration = start + " ~ " + end;

    // 3. 예산 포맷
    String budget = "₩" + new DecimalFormat("#,###").format(project.get(PROJECT.BUDGET));

    // 4. 기술스택 목록
    List<String> techStacks =
        dsl.selectDistinct(TECH_STACK.TECH_STACK_NAME)
            .from(PROJECT_AND_JOB)
            .join(JOB_AND_TECH_STACK)
            .on(PROJECT_AND_JOB.PROJECT_AND_JOB_ID.eq(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID))
            .join(TECH_STACK)
            .on(JOB_AND_TECH_STACK.TECH_STACK_NAME.eq(TECH_STACK.TECH_STACK_NAME))
            .where(PROJECT_AND_JOB.PROJECT_CODE.eq(projectCode))
            .fetchInto(String.class);

    // 5. 스쿼드 구성원
    List<ProjectDetailResponse.SquadMemberInfo> members =
        dsl.select(
                SQUAD_EMPLOYEE.IS_LEADER, // 리더 여부 포함
                MEMBER.PROFILE_IMAGE_URL,
                MEMBER.EMPLOYEE_NAME,
                PROJECT_AND_JOB.JOB_NAME)
            .from(SQUAD)
            .join(SQUAD_EMPLOYEE)
            .on(SQUAD.SQUAD_CODE.eq(SQUAD_EMPLOYEE.SQUAD_CODE))
            .join(MEMBER)
            .on(
                SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                    MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
            .join(PROJECT_AND_JOB)
            .on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .where(SQUAD.PROJECT_CODE.eq(projectCode))
            .orderBy(SQUAD_EMPLOYEE.IS_LEADER.desc()) // 리더 먼저 정렬
            .fetch()
            .map(
                r ->
                    new ProjectDetailResponse.SquadMemberInfo(
                        Integer.valueOf(r.get(SQUAD_EMPLOYEE.IS_LEADER)),
                        r.get(MEMBER.PROFILE_IMAGE_URL),
                        r.get(MEMBER.EMPLOYEE_NAME),
                        r.get(PROJECT_AND_JOB.JOB_NAME)));

    // 상태 추출 및 반환에 포함
    String status = String.valueOf(project.get(PROJECT.STATUS));

    return new ProjectDetailResponse(
        project.get(PROJECT.TITLE),
        project.get(PROJECT.DOMAIN_NAME),
        project.get(PROJECT.REQUEST_SPECIFICATION_URL),
        project.get(PROJECT.DESCRIPTION),
        duration,
        budget,
        techStacks,
        members,
        status);
  }

  public ProjectInfoDto findProjectInfoByWorkId(Long workId) {
    return dsl.select(
            DEVELOPER_PROJECT_WORK.PROJECT_CODE,
            PROJECT.TITLE,
            PROJECT.START_DATE,
            DSL.coalesce(PROJECT.ACTUAL_END_DATE, PROJECT.EXPECTED_END_DATE).as("end_date"))
        .from(DEVELOPER_PROJECT_WORK)
        .join(PROJECT)
        .on(DEVELOPER_PROJECT_WORK.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
        .where(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID.eq(workId))
        .fetchOne(
            r ->
                new ProjectInfoDto(
                    r.get(DEVELOPER_PROJECT_WORK.PROJECT_CODE),
                    r.get(PROJECT.TITLE),
                    r.get(PROJECT.START_DATE),
                    r.get("end_date", LocalDate.class)));
  }

  public WorkInfoQueryDto findById(Long projectWorkId) {
    Record record =
        dsl.select(
                DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID.as("workId"),
                DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER.as("employeeId"),
                DEVELOPER_PROJECT_WORK.PROJECT_CODE.as("projectCode"),
                PROJECT.TITLE.as("projectTitle"),
                DEVELOPER_PROJECT_WORK.APPROVAL_STATUS.as("approvalStatus"),
                DEVELOPER_PROJECT_WORK.APPROVED_AT.as("approvedAt"),
                DEVELOPER_PROJECT_WORK.CREATED_AT.as("createdAt"),
                PROJECT.ACTUAL_END_DATE.as("actualEndDate"))
            .from(DEVELOPER_PROJECT_WORK)
            .join(PROJECT)
            .on(PROJECT.PROJECT_CODE.eq(DEVELOPER_PROJECT_WORK.PROJECT_CODE))
            .where(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID.eq(projectWorkId))
            .fetchOne();

    if (record == null) return null;

    Result<
            Record7<
                Long,
                String,
                String,
                Integer,
                Integer,
                DeveloperProjectWorkHistoryFunctionType,
                DeveloperProjectWorkHistoryComplexity>>
        historyRecords =
            dsl.select(
                    DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID,
                    DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_NAME,
                    DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_DESCRIPTION,
                    DEVELOPER_PROJECT_WORK_HISTORY.DET,
                    DEVELOPER_PROJECT_WORK_HISTORY.FTR,
                    DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE,
                    DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY)
                .from(DEVELOPER_PROJECT_WORK_HISTORY)
                .where(DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_ID.eq(projectWorkId))
                .fetch();

    List<WorkInfoQueryDto.WorkRequestHistoryDto> histories =
        historyRecords.stream()
            .map(
                h -> {
                  Long historyId =
                      h.get(DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID);

                  List<String> techStackNames =
                      dsl.select(DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK.TECH_STACK_NAME)
                          .from(DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK)
                          .where(
                              DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK
                                  .DEVELOPER_PROJECT_WORK_HISTORY_ID.eq(historyId))
                          .fetchInto(String.class);

                  return new WorkInfoQueryDto.WorkRequestHistoryDto(
                      historyId,
                      h.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_NAME),
                      h.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_DESCRIPTION),
                      techStackNames,
                      Optional.ofNullable(h.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE))
                          .map(Enum::name)
                          .orElse(null),
                      h.get(DEVELOPER_PROJECT_WORK_HISTORY.DET),
                      h.get(DEVELOPER_PROJECT_WORK_HISTORY.FTR),
                      Optional.ofNullable(h.get(DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY))
                          .map(Enum::name)
                          .orElse(null));
                })
            .collect(Collectors.toList());

    return WorkInfoQueryDto.builder()
        .workId(record.get("workId", Long.class))
        .employeeId(record.get("employeeId", String.class))
        .projectCode(record.get("projectCode", String.class))
        .projectTitle(record.get("projectTitle", String.class))
        .approvalStatus(record.get("approvalStatus", String.class))
        .approvedAt(record.get("approvedAt", LocalDateTime.class))
        .createdAt(record.get("createdAt", LocalDateTime.class))
        .actualEndDate(record.get("actualEndDate", LocalDate.class))
        .histories(histories)
        .build();
  }
}
