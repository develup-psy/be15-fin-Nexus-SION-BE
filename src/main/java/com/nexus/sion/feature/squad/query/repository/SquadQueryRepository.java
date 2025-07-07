package com.nexus.sion.feature.squad.query.repository;

import static com.example.jooq.generated.Tables.*;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.ProjectAndJob.PROJECT_AND_JOB;
import static com.example.jooq.generated.tables.Squad.SQUAD;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.jooq.generated.Tables;
import com.example.jooq.generated.tables.ProjectAndJob;
import com.nexus.sion.feature.squad.query.dto.response.SquadDetailResponse;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.SquadOriginType;
import com.example.jooq.generated.tables.records.SquadRecord;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
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

    public SquadDetailResponse fetchSquadDetail(String squadCode) {
        var squad = dsl.selectFrom(SQUAD)
                .where(SQUAD.SQUAD_CODE.eq(squadCode))
                .fetchOne();

        if (squad == null){
            throw new BusinessException(ErrorCode.SQUAD_DETAIL_NOT_FOUND);
        }

        var employeeRecords = dsl
                .select(
                        MEMBER.EMPLOYEE_NAME,
                        PROJECT_AND_JOB.JOB_NAME,
                        MEMBER.GRADE_CODE,
                        GRADE.MONTHLY_UNIT_PRICE,
                        MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                        GRADE.PRODUCTIVITY
                )
                .from(SQUAD_EMPLOYEE)
                .join(MEMBER).on(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER.eq(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
                .join(GRADE).on(MEMBER.GRADE_CODE.cast(VARCHAR).eq(GRADE.GRADE_CODE.cast(VARCHAR)))
                .join(PROJECT_AND_JOB).on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
                .where(SQUAD_EMPLOYEE.SQUAD_CODE.eq(squadCode))
                .fetch();

        var techStacks =  dsl.selectDistinct(
                        JOB_AND_TECH_STACK.TECH_STACK_NAME)
                .from(SQUAD_EMPLOYEE)
                .join(ProjectAndJob.PROJECT_AND_JOB).on(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID.eq(ProjectAndJob.PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
                .join(JOB_AND_TECH_STACK).on(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID.eq(PROJECT_AND_JOB.PROJECT_AND_JOB_ID))
                .where(SQUAD_EMPLOYEE.SQUAD_CODE.eq(squadCode))
                .fetch()
                .getValues(JOB_AND_TECH_STACK.TECH_STACK_NAME);

        List<SquadDetailResponse.MemberDetail> members = employeeRecords.stream()
                .map(r -> SquadDetailResponse.MemberDetail.builder()
                        .name(r.get(MEMBER.EMPLOYEE_NAME))
                        .job(r.get(PROJECT_AND_JOB.JOB_NAME))
                        .grade(r.get(MEMBER.GRADE_CODE).name())
                        .monthlyUnitPrice(r.get(GRADE.MONTHLY_UNIT_PRICE))
                        .memberId(r.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
                        .productivity(r.get(GRADE.PRODUCTIVITY))
                        .build()
                ).toList();

        Map<String, Integer> memberCountByJob = employeeRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> r.get(PROJECT_AND_JOB.JOB_NAME),
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));

        Map<String, Integer> gradeCount = employeeRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> r.get(MEMBER.GRADE_CODE).name(),
                        Collectors.reducing(0, e -> 1, Integer::sum)
                ));

        int estimatedDuration = Optional.ofNullable(squad.get(SQUAD.ESTIMATED_DURATION))
                .map(BigDecimal::intValue)
                .orElse(0);

        int totalCost = Optional.ofNullable(squad.get(Tables.SQUAD.ESTIMATED_COST))
                .map(BigDecimal::intValue)
                .orElse(0);

        Boolean isActive = squad.get(SQUAD.IS_ACTIVE) == 1 ? Boolean.TRUE : Boolean.FALSE;

        return SquadDetailResponse.builder()
                .squadCode(squad.get(SQUAD.SQUAD_CODE))
                .title(squad.get(SQUAD.TITLE))
                .recommendationReason(squad.get(SQUAD.RECOMMENDATION_REASON))
                .totalMemberCount(members.size())
                .memberCountByJob(memberCountByJob)
                .gradeCount(gradeCount)
                .techStacks(techStacks)
                .estimatedDuration(estimatedDuration)
                .totalCost(totalCost)
                .members(members)
                .isActive(isActive)
                .description(squad.get(SQUAD.DESCRIPTION))
                .origin(squad.get(SQUAD.ORIGIN_TYPE))
                .build();
    }
}
