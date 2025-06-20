package com.nexus.sion.feature.squad.query.repository;

import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.ProjectAndJob.PROJECT_AND_JOB;
import static com.example.jooq.generated.tables.Squad.SQUAD;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.example.jooq.generated.enums.SquadOriginType;
import org.jooq.*;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SquadQueryRepository {

  private final DSLContext dsl;

  public List<SquadListResponse> findSquads(SquadListRequest request) {
    String projectCode = request.getProjectCode();

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

    return dsl
        .selectFrom(SQUAD)
        .where(SQUAD.PROJECT_CODE.eq(projectCode))
        .orderBy(SQUAD.CREATED_AT.desc())
        .fetch()
        .stream()
        .map(
            r -> {
              String code = r.get(SQUAD.SQUAD_CODE);
              String name = r.get(SQUAD.TITLE);

              SquadOriginType originType = r.get(SQUAD.ORIGIN_TYPE);
              boolean isAiRecommended = SquadOriginType.AI.equals(originType);

              LocalDate start = r.get(SQUAD.CREATED_AT).toLocalDate();
              LocalDate end = start.plusMonths(r.get(SQUAD.ESTIMATED_DURATION).longValue());
              String period = start + " ~ " + end;

              DecimalFormat decimalFormat = new DecimalFormat("#,###");
              String cost = "₩" + decimalFormat.format(r.get(SQUAD.ESTIMATED_COST));

              return new SquadListResponse(
                  code,
                      name,
                      isAiRecommended,
                      memberMap.getOrDefault(code, List.of()),
                      period, cost
              );
            })
        .toList();
  }
}
