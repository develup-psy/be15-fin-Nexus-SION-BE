package com.nexus.sion.feature.project.query.repository;

import com.example.jooq.generated.enums.MemberGradeCode;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.JobAndTechStack.JOB_AND_TECH_STACK;
import static com.example.jooq.generated.tables.Squad.SQUAD;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.Grade.GRADE;
import static com.example.jooq.generated.tables.Project.PROJECT;
import static org.jooq.impl.SQLDataType.VARCHAR;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ReplacementRecommendationRepository {

    private final DSLContext dsl;

    public List<DeveloperSummary> findCandidatesForReplacement(String projectCode, String employeeId) {
        // 1. 스쿼드 조회
        Record1<String> squadCodeRecord = dsl
                .select(SQUAD.SQUAD_CODE)
                .from(SQUAD)
                .where(SQUAD.PROJECT_CODE.eq(projectCode))
                .and(SQUAD.IS_ACTIVE.isTrue())
                .fetchOne();

        log.info("[1] SquadCodeRecord: {}", squadCodeRecord);

        if (squadCodeRecord == null) {
            throw new IllegalArgumentException("활성화된 스쿼드를 찾을 수 없습니다.");
        }

        String squadCode = squadCodeRecord.value1();

        // 2. 참여 직무 조회
        Record1<Long> projectAndJobIdRecord = dsl
                .select(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID)
                .from(SQUAD_EMPLOYEE)
                .where(SQUAD_EMPLOYEE.SQUAD_CODE.eq(squadCode))
                .and(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER.eq(employeeId))
                .fetchOne();

        log.info("[2] ProjectAndJobIdRecord: {}", projectAndJobIdRecord);

        if (projectAndJobIdRecord == null) {
            throw new IllegalArgumentException("직무를 찾을 수 없습니다.");
        }

        Long projectAndJobId = projectAndJobIdRecord.value1();

        // 3. 해당 직무에 필요한 기술스택 조회
        List<String> requiredStacks = dsl
                .select(JOB_AND_TECH_STACK.TECH_STACK_NAME)
                .from(JOB_AND_TECH_STACK)
                .where(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID.eq(projectAndJobId))
                .fetchInto(String.class);

        log.info("[3] Required TechStacks: {}", requiredStacks);

        if (requiredStacks.isEmpty()) {
            throw new IllegalArgumentException("직무에 필요한 기술스택이 없습니다.");
        }

        String domainName = dsl
                .select(PROJECT.DOMAIN_NAME)
                .from(PROJECT)
                .where(PROJECT.PROJECT_CODE.eq(projectCode))
                .fetchOne(PROJECT.DOMAIN_NAME);

        log.info("[4] domainName: {}", domainName);

        // 4. 모든 기술스택을 포함하는 후보 개발자 조회용 임시 테이블
        Table<?> requiredTechCount = dsl
                .select(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.as("EMPLOYEE_IDENTIFICATION_NUMBER"))
                .from(DEVELOPER_TECH_STACK)
                .where(DEVELOPER_TECH_STACK.TECH_STACK_NAME.in(requiredStacks))
                .groupBy(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER)
                .having(DSL.countDistinct(DEVELOPER_TECH_STACK.TECH_STACK_NAME).eq(requiredStacks.size()))
                .asTable("required_developers");

        log.info(">> field exists? {}", requiredTechCount.field("EMPLOYEE_IDENTIFICATION_NUMBER", String.class) != null);


        List<String> requiredDevs = dsl
                .select(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER)
                .from(DEVELOPER_TECH_STACK)
                .where(DEVELOPER_TECH_STACK.TECH_STACK_NAME.in(requiredStacks))
                .groupBy(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER)
                .having(DSL.countDistinct(DEVELOPER_TECH_STACK.TECH_STACK_NAME).eq(requiredStacks.size()))
                .fetchInto(String.class);

        log.info(">> Required Developer Count: {}", requiredDevs.size());
        log.info(">> Required Developers: {}", requiredDevs);


        Result<? extends Record> records = dsl
                .select(
                        MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                        MEMBER.EMPLOYEE_NAME,
                        MEMBER.GRADE_CODE,
                        DSL.avg(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES),
                        DSL.coalesce(DSL.countDistinct(
                                DSL.when(PROJECT.DOMAIN_NAME.eq(domainName), SQUAD_EMPLOYEE.SQUAD_CODE)
                        ), 0),
                        GRADE.PRODUCTIVITY,
                        GRADE.MONTHLY_UNIT_PRICE
                )
                .from(MEMBER)
                .join(GRADE).on(MEMBER.GRADE_CODE.cast(VARCHAR).eq(GRADE.GRADE_CODE.cast(VARCHAR)))
                .join(DEVELOPER_TECH_STACK).on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER))
                .join(requiredTechCount).on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(requiredTechCount.field("EMPLOYEE_IDENTIFICATION_NUMBER", String.class)))
                .leftJoin(SQUAD_EMPLOYEE).on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER))
                .leftJoin(SQUAD).on(SQUAD_EMPLOYEE.SQUAD_CODE.eq(SQUAD.SQUAD_CODE))
                .leftJoin(PROJECT).on(SQUAD.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
                .where(MEMBER.STATUS.eq(MemberStatus.AVAILABLE))
                .groupBy(
                        MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                        MEMBER.EMPLOYEE_NAME,
                        MEMBER.GRADE_CODE,
                        GRADE.PRODUCTIVITY,
                        GRADE.MONTHLY_UNIT_PRICE
                )
                .fetch();

        for (var record : records) {
            log.info(">> Record: {}", record);
        }


        // 5. 최종 후보군 상세 정보 조회
        List<DeveloperSummary> candidates = dsl
                .select(
                        MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.as("id"),
                        MEMBER.EMPLOYEE_NAME.as("name"),
                        MEMBER.GRADE_CODE.as("grade"),
                        DSL.avg(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES).as("avgTechScore"),
                        DSL.coalesce(DSL.countDistinct(
                                DSL.when(PROJECT.DOMAIN_NAME.eq(domainName), SQUAD_EMPLOYEE.SQUAD_CODE)
                        ), 0).as("domainCount"),
                        GRADE.PRODUCTIVITY,
                        GRADE.MONTHLY_UNIT_PRICE
                )
                .from(MEMBER)
                .join(GRADE).on(MEMBER.GRADE_CODE.cast(VARCHAR).eq(GRADE.GRADE_CODE.cast(VARCHAR)))
                .join(DEVELOPER_TECH_STACK).on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER))
                .join(requiredTechCount).on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(requiredTechCount.field("EMPLOYEE_IDENTIFICATION_NUMBER", String.class)))
                .leftJoin(SQUAD_EMPLOYEE).on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER))
                .leftJoin(SQUAD).on(SQUAD_EMPLOYEE.SQUAD_CODE.eq(SQUAD.SQUAD_CODE))
                .leftJoin(PROJECT).on(SQUAD.PROJECT_CODE.eq(PROJECT.PROJECT_CODE))
                .where(MEMBER.STATUS.eq(MemberStatus.AVAILABLE))
                .groupBy(
                        MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                        MEMBER.EMPLOYEE_NAME,
                        MEMBER.GRADE_CODE,
                        GRADE.PRODUCTIVITY,
                        GRADE.MONTHLY_UNIT_PRICE
                )
                .fetch(record -> DeveloperSummary.builder()
                        .id(record.get("id", String.class))
                        .name(record.get("name", String.class))
                        .grade(record.get("grade", String.class))
                        .avgTechScore(record.get("avgTechScore", Double.class))
                        .domainCount(record.get("domainCount", Integer.class))
                        .productivity(record.get(GRADE.PRODUCTIVITY))
                        .monthlyUnitPrice(record.get(GRADE.MONTHLY_UNIT_PRICE))
                        .build());

        log.info("[5] Final Developer Candidates: {}", candidates);

        return candidates;
    }
}
