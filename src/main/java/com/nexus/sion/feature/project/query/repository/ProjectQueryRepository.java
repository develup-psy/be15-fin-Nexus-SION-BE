package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.Tables.DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK;
import static com.example.jooq.generated.tables.DeveloperProjectWork.DEVELOPER_PROJECT_WORK;
import static com.example.jooq.generated.tables.DeveloperProjectWorkHistory.DEVELOPER_PROJECT_WORK_HISTORY;
import static com.example.jooq.generated.tables.JobAndTechStack.JOB_AND_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.Project.PROJECT;
import static com.example.jooq.generated.tables.ProjectAndJob.PROJECT_AND_JOB;
import static com.example.jooq.generated.tables.Squad.SQUAD;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;
import static com.example.jooq.generated.tables.TechStack.TECH_STACK;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.*;
import com.example.jooq.generated.tables.pojos.Project;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectDetailResponse;
import com.nexus.sion.feature.project.query.dto.response.ProjectInfoDto;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;
import com.nexus.sion.feature.project.query.dto.response.WorkInfoQueryDto;

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
                      record.get(PROJECT.NUMBER_OF_MEMBERS),
                      record.get(PROJECT.ANALYSIS_STATUS));
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
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                SQUAD_EMPLOYEE.IS_LEADER, // ✅ 리더 여부 포함
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
            .and(SQUAD.IS_ACTIVE.eq((byte) 1))
            .orderBy(SQUAD_EMPLOYEE.IS_LEADER.desc()) // 리더 먼저 정렬
            .fetch()
            .map(
                r ->
                    new ProjectDetailResponse.SquadMemberInfo(
                        r.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER),
                        Integer.valueOf(r.get(SQUAD_EMPLOYEE.IS_LEADER)), // 👈 여기로 포함
                        r.get(MEMBER.PROFILE_IMAGE_URL),
                        r.get(MEMBER.EMPLOYEE_NAME),
                        r.get(PROJECT_AND_JOB.JOB_NAME)));

    // ✅ 상태 추출 및 반환에 포함
    String status = String.valueOf(project.get(PROJECT.STATUS));
    ProjectAnalysisStatus analysisStatus = project.get(PROJECT.ANALYSIS_STATUS);

    String squadCode =
        dsl.select(SQUAD.SQUAD_CODE)
            .from(SQUAD)
            .where(SQUAD.PROJECT_CODE.eq(projectCode))
            .and(SQUAD.IS_ACTIVE.eq((byte) 1))
            .fetchOne(SQUAD.SQUAD_CODE);

    return new ProjectDetailResponse(
        project.get(PROJECT.TITLE),
        project.get(PROJECT.DOMAIN_NAME),
        project.get(PROJECT.REQUEST_SPECIFICATION_URL),
        project.get(PROJECT.DESCRIPTION),
        duration,
        budget,
        techStacks,
        members,
        status,
        analysisStatus, // ✅ 여기 포함,
        squadCode);
  }

  public PageResponse<ProjectListResponse> findProjectListByMemberId(
      String employeeId, int page, int size) {
    // 1. 해당 사원의 참여 project_code 목록 조회
    List<String> projectCodes =
        dsl.selectDistinct(PROJECT_AND_JOB.PROJECT_CODE)
            .from(SQUAD_EMPLOYEE)
            .join(PROJECT_AND_JOB)
            .on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .where(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
            .fetchInto(String.class);

    if (projectCodes.isEmpty()) {
      return PageResponse.fromJooq(List.of(), 0, page, size);
    }

    // 2. 전체 개수
    long totalCount =
        dsl.selectCount()
            .from(PROJECT)
            .where(PROJECT.PROJECT_CODE.in(projectCodes).and(PROJECT.DELETED_AT.isNull()))
            .fetchOne(0, long.class);

    // 3. 목록 조회
    List<ProjectListResponse> content =
        dsl
            .selectFrom(PROJECT)
            .where(PROJECT.PROJECT_CODE.in(projectCodes).and(PROJECT.DELETED_AT.isNull()))
            .orderBy(PROJECT.CREATED_AT.desc())
            .limit(size)
            .offset(page * size)
            .fetch()
            .stream()
            .map(
                record -> {
                  LocalDate start = record.get(PROJECT.START_DATE);
                  LocalDate end =
                      record.get(PROJECT.ACTUAL_END_DATE) != null
                          ? record.get(PROJECT.ACTUAL_END_DATE)
                          : record.get(PROJECT.EXPECTED_END_DATE);
                  int months = (int) ChronoUnit.MONTHS.between(start, end);
                  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

                  return new ProjectListResponse(
                      record.get(PROJECT.PROJECT_CODE),
                      record.get(PROJECT.TITLE),
                      record.get(PROJECT.DESCRIPTION),
                      formatter.format(start),
                      formatter.format(end),
                      months,
                      String.valueOf(record.get(PROJECT.STATUS)),
                      record.get(PROJECT.DOMAIN_NAME),
                      record.get(PROJECT.NUMBER_OF_MEMBERS),
                      record.get(PROJECT.ANALYSIS_STATUS));
                })
            .collect(Collectors.toList());

    return PageResponse.fromJooq(content, totalCount, page, size);
  }

  public ProjectDetailResponse findProjectDetailByMemberIdAndProjectCode(
      String employeeId, String projectCode) {
    // 해당 사원이 해당 프로젝트에 참여했는지 확인
    boolean exists =
        dsl.selectOne()
            .from(SQUAD_EMPLOYEE)
            .join(PROJECT_AND_JOB)
            .on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .where(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
            .and(PROJECT_AND_JOB.PROJECT_CODE.eq(projectCode))
            .fetchOptional()
            .isPresent();

    if (!exists) {
      throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND); // 또는 권한 없음 에러
    }

    // 기존 상세조회 재활용
    return getProjectDetail(projectCode);
  }

  public ProjectInfoDto findProjectInfoByWorkId(Long workId) {
    Record record =
        dsl.select(
                PROJECT.PROJECT_CODE,
                PROJECT.TITLE,
                PROJECT.START_DATE,
                DSL.coalesce(PROJECT.ACTUAL_END_DATE, PROJECT.EXPECTED_END_DATE).as("end_date"),
                DEVELOPER_PROJECT_WORK.APPROVAL_STATUS)
            .from(DEVELOPER_PROJECT_WORK)
            .join(PROJECT)
            .on(DEVELOPER_PROJECT_WORK.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
            .where(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID.eq(workId))
            .fetchOne();

    if (record == null) {
      throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
    }

    return new ProjectInfoDto(
        record.get(PROJECT.PROJECT_CODE),
        record.get(PROJECT.TITLE),
        record.get(PROJECT.START_DATE),
        record.get("end_date", LocalDate.class),
        record.get(
            DEVELOPER_PROJECT_WORK.APPROVAL_STATUS, DeveloperProjectWork.ApprovalStatus.class));
  }

  public WorkInfoQueryDto findById(Long projectWorkId) {
    // 1. 프로젝트 이력 기본 정보 조회
    Record work =
        dsl.select(
                DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID,
                DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER,
                DEVELOPER_PROJECT_WORK.PROJECT_CODE,
                PROJECT.TITLE.as("project_title"),
                DEVELOPER_PROJECT_WORK.APPROVAL_STATUS,
                DEVELOPER_PROJECT_WORK.REJECTED_REASON,
                DEVELOPER_PROJECT_WORK.APPROVED_AT,
                DEVELOPER_PROJECT_WORK.CREATED_AT,
                PROJECT.ACTUAL_END_DATE)
            .from(DEVELOPER_PROJECT_WORK)
            .join(PROJECT)
            .on(DEVELOPER_PROJECT_WORK.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
            .where(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID.eq(projectWorkId))
            .fetchOne();

    if (work == null) {
      throw new BusinessException(ErrorCode.WORK_NOT_FOUND);
    }

    // 2. 기능 히스토리 + 기술스택 목록 조회 (fetchGroups 사용 안 함)
    Result<? extends Record> records =
        dsl.select(
                DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID,
                DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_NAME,
                DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_DESCRIPTION,
                DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE,
                DEVELOPER_PROJECT_WORK_HISTORY.DET,
                DEVELOPER_PROJECT_WORK_HISTORY.FTR,
                DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY,
                DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK.TECH_STACK_NAME)
            .from(DEVELOPER_PROJECT_WORK_HISTORY)
            .leftJoin(DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK)
            .on(
                DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID.eq(
                    DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK.DEVELOPER_PROJECT_WORK_HISTORY_ID))
            .where(DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_ID.eq(projectWorkId))
            .fetch();

    Map<Long, List<Record>> grouped =
        records.stream()
            .collect(
                Collectors.groupingBy(
                    r -> r.get(DEVELOPER_PROJECT_WORK_HISTORY.DEVELOPER_PROJECT_WORK_HISTORY_ID)));

    List<WorkInfoQueryDto.WorkRequestHistoryDto> histories =
        grouped.entrySet().stream()
            .map(
                entry -> {
                  Record any = entry.getValue().get(0);

                  List<String> techStacks =
                      entry.getValue().stream()
                          .map(
                              r -> r.get(DEVELOPER_PROJECT_WORK_HISTORY_TECH_STACK.TECH_STACK_NAME))
                          .filter(Objects::nonNull)
                          .distinct()
                          .collect(Collectors.toList());

                  DeveloperProjectWorkHistoryFunctionType functionType =
                      any.get(
                          DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_TYPE,
                          DeveloperProjectWorkHistoryFunctionType.class);

                  DeveloperProjectWorkHistoryComplexity complexity =
                      any.get(
                          DEVELOPER_PROJECT_WORK_HISTORY.COMPLEXITY,
                          DeveloperProjectWorkHistoryComplexity.class);

                  return new WorkInfoQueryDto.WorkRequestHistoryDto(
                      entry.getKey(),
                      any.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_NAME),
                      any.get(DEVELOPER_PROJECT_WORK_HISTORY.FUNCTION_DESCRIPTION),
                      techStacks,
                      functionType != null ? functionType.name() : null,
                      any.get(DEVELOPER_PROJECT_WORK_HISTORY.DET),
                      any.get(DEVELOPER_PROJECT_WORK_HISTORY.FTR),
                      complexity != null ? complexity.name() : null);
                })
            .collect(Collectors.toList());

    DeveloperProjectWorkApprovalStatus approvalStatus =
        work.get(DEVELOPER_PROJECT_WORK.APPROVAL_STATUS, DeveloperProjectWorkApprovalStatus.class);

    return WorkInfoQueryDto.builder()
        .workId(work.get(DEVELOPER_PROJECT_WORK.DEVELOPER_PROJECT_WORK_ID))
        .employeeId(work.get(DEVELOPER_PROJECT_WORK.EMPLOYEE_IDENTIFICATION_NUMBER))
        .projectCode(work.get(DEVELOPER_PROJECT_WORK.PROJECT_CODE))
        .projectTitle(work.get("project_title", String.class))
        .approvalStatus(approvalStatus != null ? approvalStatus.name() : null)
        .rejectedReason(work.get(DEVELOPER_PROJECT_WORK.REJECTED_REASON))
        .approvedAt(work.get(DEVELOPER_PROJECT_WORK.APPROVED_AT))
        .createdAt(work.get(DEVELOPER_PROJECT_WORK.CREATED_AT))
        .actualEndDate(work.get(PROJECT.ACTUAL_END_DATE, LocalDate.class))
        .histories(histories)
        .build();
  }

  public List<Project> findProjectsByEmployeeId(
      String employeeId, List<String> statuses, int page, int size) {
    // 기본 조건: 참여 중인 프로젝트 + 삭제되지 않은 프로젝트
    Condition condition =
        PROJECT
            .DELETED_AT
            .isNull()
            .and(
                PROJECT.PROJECT_CODE.in(
                    DSL.select(PROJECT_AND_JOB.PROJECT_CODE)
                        .from(PROJECT_AND_JOB)
                        .join(SQUAD_EMPLOYEE)
                        .on(
                            PROJECT_AND_JOB.PROJECT_AND_JOB_ID.eq(
                                SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID))
                        .where(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))));

    // status 필터링
    if (statuses != null && !statuses.isEmpty()) {
      condition =
          condition.and(PROJECT.STATUS.in(statuses.stream().map(ProjectStatus::valueOf).toList()));
    }

    return dsl.select(PROJECT.fields())
        .from(PROJECT)
        .where(condition)
        .orderBy(PROJECT.START_DATE.desc())
        .limit(size)
        .offset(page * size)
        .fetchInto(Project.class);
  }

  public long countProjectsByEmployeeId(String employeeId, List<String> statuses) {
    Condition condition =
        PROJECT
            .DELETED_AT
            .isNull()
            .and(
                PROJECT.PROJECT_CODE.in(
                    DSL.select(PROJECT_AND_JOB.PROJECT_CODE)
                        .from(PROJECT_AND_JOB)
                        .join(SQUAD_EMPLOYEE)
                        .on(
                            PROJECT_AND_JOB.PROJECT_AND_JOB_ID.eq(
                                SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID))
                        .where(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))));

    if (statuses != null && !statuses.isEmpty()) {
      condition =
          condition.and(PROJECT.STATUS.in(statuses.stream().map(ProjectStatus::valueOf).toList()));
    }

    return dsl.selectCount().from(PROJECT).where(condition).fetchOne(0, Long.class);
  }
}
