package com.nexus.sion.feature.squad.query.repository;

import static com.example.jooq.generated.Tables.*;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.ProjectAndJob.PROJECT_AND_JOB;
import static com.example.jooq.generated.tables.Squad.SQUAD;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nexus.sion.common.dto.PageResponse;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.SquadOriginType;
import com.example.jooq.generated.tables.records.SquadRecord;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SquadQueryRepository {

  private final DSLContext dsl;

  public PageResponse<SquadListResponse> findSquads(SquadListRequest request) {
    String projectCode = request.getProjectCode();
    int page = request.getPage();
    int size = request.getSize();

    Map<String, List<SquadListResponse.MemberInfo>> memberMap =
        dsl.select(SQUAD_EMPLOYEE.SQUAD_CODE, MEMBER.EMPLOYEE_NAME, PROJECT_AND_JOB.JOB_NAME)
            .from(SQUAD_EMPLOYEE)
            .join(MEMBER)
            .on(
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                    SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER))
            .join(PROJECT_AND_JOB)
            .on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .fetchGroups(
                r -> r.get(SQUAD_EMPLOYEE.SQUAD_CODE),
                r ->
                    new SquadListResponse.MemberInfo(
                        r.get(MEMBER.EMPLOYEE_NAME), r.get(PROJECT_AND_JOB.JOB_NAME)));

    Result<SquadRecord> records =
        dsl.selectFrom(SQUAD)
            .where(SQUAD.PROJECT_CODE.eq(projectCode))
            .orderBy(SQUAD.CREATED_AT.desc())
            .limit(size)
            .offset(page * size)
            .fetch();

    Long total =
        dsl.selectCount()
            .from(SQUAD)
            .where(SQUAD.PROJECT_CODE.eq(projectCode))
            .fetchOne(0, Long.class);

    List<SquadListResponse> content =
        records.stream()
            .map(
                r -> {
                  String code = r.get(SQUAD.SQUAD_CODE);
                  String name = r.get(SQUAD.TITLE);

                  SquadOriginType originType = r.get(SQUAD.ORIGIN_TYPE);
                  boolean isAiRecommended = SquadOriginType.AI.equals(originType);

                  LocalDate start = r.get(SQUAD.CREATED_AT).toLocalDate();

                  // null일 경우 0개월 처리
                  BigDecimal duration = r.get(SQUAD.ESTIMATED_DURATION);
                  long durationValue = duration != null ? duration.longValue() : 0L;
                  LocalDate end = start.plusMonths(durationValue);
                  String period = start + " ~ " + end;

                  DecimalFormat decimalFormat = new DecimalFormat("#,###");

                  // null일 경우 0원 처리
                  BigDecimal estimatedCost = r.get(SQUAD.ESTIMATED_COST);
                  String cost =
                      "₩"
                          + decimalFormat.format(
                              estimatedCost != null ? estimatedCost : BigDecimal.ZERO);

                  return new SquadListResponse(
                      code,
                      name,
                      isAiRecommended,
                      memberMap.getOrDefault(code, List.of()),
                      period,
                      cost);
                })
            .toList();


      return PageResponse.fromJooq(content, total != null ? total : 0L, page, size);
  }

  public SquadDetailResponse findSquadDetailByCode(String squadCode) {
    Record squadRecord = dsl.selectFrom(SQUAD).where(SQUAD.SQUAD_CODE.eq(squadCode)).fetchOne();

    if (squadRecord == null) {
      throw new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND);
    }

    boolean aiRecommended = SquadOriginType.AI.equals(squadRecord.get(SQUAD.ORIGIN_TYPE));

    BigDecimal duration = squadRecord.get(SQUAD.ESTIMATED_DURATION);
    BigDecimal safeDuration = duration != null ? duration : BigDecimal.ZERO;

    DecimalFormat format = new DecimalFormat("0.##"); // 소수점 1자리까지만 표시 (예: 3.5개월)
    String estimatedPeriod = format.format(safeDuration) + "개월";

    // 비용 세부내역용 records
    var records =
        dsl.select(
                MEMBER.EMPLOYEE_NAME,
                PROJECT_AND_JOB.JOB_NAME,
                MEMBER.GRADE_CODE,
                GRADE.MONTHLY_UNIT_PRICE)
            .from(SQUAD_EMPLOYEE)
            .join(MEMBER)
            .on(
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                    SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER))
            .join(PROJECT_AND_JOB)
            .on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .join(GRADE)
            .on(MEMBER.GRADE_CODE.cast(String.class).eq(GRADE.GRADE_CODE.cast(String.class)))
            .where(SQUAD_EMPLOYEE.SQUAD_CODE.eq(squadCode))
            .fetch();

    // 개발자 단가 총합 계산
    BigDecimal totalCost =
        records.stream()
            .map(
                r -> {
                  Integer monthlyUnitPrice = r.get(GRADE.MONTHLY_UNIT_PRICE);
                  BigDecimal price =
                      monthlyUnitPrice != null
                          ? BigDecimal.valueOf(monthlyUnitPrice)
                          : BigDecimal.ZERO;
                  return price.multiply(safeDuration);
                })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    String estimatedCost = "₩" + decimalFormat.format(totalCost);

    // 구성원 조회
    List<SquadDetailResponse.MemberInfo> members =
        dsl.select(
                SQUAD_EMPLOYEE.IS_LEADER,
                MEMBER.PROFILE_IMAGE_URL,
                PROJECT_AND_JOB.JOB_NAME,
                MEMBER.EMPLOYEE_NAME)
            .from(SQUAD_EMPLOYEE)
            .join(MEMBER)
            .on(
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                    SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER))
            .join(PROJECT_AND_JOB)
            .on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .where(SQUAD_EMPLOYEE.SQUAD_CODE.eq(squadCode))
            .fetch()
            .map(
                r ->
                    new SquadDetailResponse.MemberInfo(
                        r.get(SQUAD_EMPLOYEE.IS_LEADER) != null
                            && r.get(SQUAD_EMPLOYEE.IS_LEADER) == 1,
                        r.get(MEMBER.PROFILE_IMAGE_URL),
                        r.get(PROJECT_AND_JOB.JOB_NAME),
                        r.get(MEMBER.EMPLOYEE_NAME)));

    // 단가 세부 정보
    List<SquadDetailResponse.CostBreakdown> costDetails =
        records.stream()
            .map(
                r -> {
                  Integer monthlyUnitPrice = r.get(GRADE.MONTHLY_UNIT_PRICE);
                  int safePrice = monthlyUnitPrice != null ? monthlyUnitPrice : 0;
                  return new SquadDetailResponse.CostBreakdown(
                      r.get(MEMBER.EMPLOYEE_NAME),
                      r.get(PROJECT_AND_JOB.JOB_NAME),
                      String.valueOf(r.get(MEMBER.GRADE_CODE)),
                      "₩" + decimalFormat.format(safePrice));
                })
            .toList();

    List<String> techStacks =
        dsl.selectDistinct(TECH_STACK.TECH_STACK_NAME)
            .from(SQUAD_EMPLOYEE)
            .join(PROJECT_AND_JOB)
            .on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
            .join(JOB_AND_TECH_STACK)
            .on(PROJECT_AND_JOB.PROJECT_AND_JOB_ID.eq(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID))
            .join(TECH_STACK)
            .on(JOB_AND_TECH_STACK.TECH_STACK_NAME.eq(TECH_STACK.TECH_STACK_NAME))
            .where(SQUAD_EMPLOYEE.SQUAD_CODE.eq(squadCode))
            .fetchInto(String.class);

    List<SquadDetailResponse.CommentResponse> comments =
        dsl.select(
                SQUAD_COMMENT.COMMENT_ID,
                SQUAD_COMMENT.EMPLOYEE_IDENTIFICATION_NUMBER,
                SQUAD_COMMENT.CONTENT,
                SQUAD_COMMENT.CREATED_AT)
            .from(SQUAD_COMMENT)
            .where(SQUAD_COMMENT.SQUAD_CODE.eq(squadCode))
            .orderBy(SQUAD_COMMENT.CREATED_AT.asc())
            .fetch()
            .map(
                r ->
                    new SquadDetailResponse.CommentResponse(
                        r.get(SQUAD_COMMENT.COMMENT_ID),
                        r.get(SQUAD_COMMENT.EMPLOYEE_IDENTIFICATION_NUMBER),
                        r.get(SQUAD_COMMENT.CONTENT),
                        r.get(SQUAD_COMMENT.CREATED_AT)));

    Map<String, Long> jobCounts =
        records.stream()
            .collect(
                Collectors.groupingBy(r -> r.get(PROJECT_AND_JOB.JOB_NAME), Collectors.counting()));

    Map<String, Long> gradeCounts =
        records.stream()
            .collect(
                Collectors.groupingBy(
                    r -> String.valueOf(r.get(MEMBER.GRADE_CODE)), Collectors.counting()));

    SquadDetailResponse.SummaryInfo summary =
        new SquadDetailResponse.SummaryInfo(jobCounts, gradeCounts);

    return new SquadDetailResponse(
        squadRecord.get(SQUAD.SQUAD_CODE),
        squadRecord.get(SQUAD.TITLE),
        aiRecommended,
        estimatedPeriod,
        estimatedCost,
        summary,
        techStacks,
        members,
        costDetails,
        squadRecord.get(SQUAD.RECOMMENDATION_REASON),
        comments);
  }

  public boolean existsByProjectCodeAndIsActive(String projectCode) {
    return dsl.fetchExists(
        dsl.selectFrom(SQUAD)
            .where(SQUAD.PROJECT_CODE.eq(projectCode))
            .and(SQUAD.IS_ACTIVE.isTrue()));
  }

  public SquadDetailResponse findConfirmedSquadByProjectCode(String projectCode) {
    String squadCode =
        dsl.select(SQUAD.SQUAD_CODE)
            .from(SQUAD)
            .where(SQUAD.PROJECT_CODE.eq(projectCode))
            .and(SQUAD.IS_ACTIVE.isTrue())
            .fetchOneInto(String.class);

    if (squadCode == null) {
      throw new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND);
    }

    return findSquadDetailByCode(squadCode);
  }
}
