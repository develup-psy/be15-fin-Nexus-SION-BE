package com.nexus.sion.feature.project.query.repository;

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
import java.util.stream.Collectors;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.ProjectAnalysisStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectDetailResponse;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;

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
            .orderBy(SQUAD_EMPLOYEE.IS_LEADER.desc()) // 리더 먼저 정렬
            .fetch()
            .map(
                r ->
                    new ProjectDetailResponse.SquadMemberInfo(
                        Integer.valueOf(r.get(SQUAD_EMPLOYEE.IS_LEADER)), // 👈 여기로 포함
                        r.get(MEMBER.PROFILE_IMAGE_URL),
                        r.get(MEMBER.EMPLOYEE_NAME),
                        r.get(PROJECT_AND_JOB.JOB_NAME)));

    // ✅ 상태 추출 및 반환에 포함
    String status = String.valueOf(project.get(PROJECT.STATUS));
    ProjectAnalysisStatus analysisStatus = project.get(PROJECT.ANALYSIS_STATUS);

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
        analysisStatus // ✅ 여기 포함
        );
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
}
