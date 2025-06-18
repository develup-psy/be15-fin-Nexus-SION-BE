package com.nexus.sion.feature.member.query.service;

import com.example.jooq.generated.enums.MemberRole;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static org.jooq.impl.DSL.*;

@RequiredArgsConstructor
@Service
public class MemberQueryService {

    private final DSLContext dsl;

    public PageResponse<MemberListResponse> getAllMembers(MemberListRequest request) {
        int page = request.getPage();
        int size = request.getSize();
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "employeeName";
        String sortDir = request.getSortDir() != null ? request.getSortDir() : "asc";

        Condition condition = MEMBER.DELETED_AT.isNull()
                .and(MEMBER.ROLE.eq(MemberRole.INSIDER));

        // 상태 필터
        if (request.getStatus() != null) {
            try {
                MemberStatus statusEnum = MemberStatus.valueOf(request.getStatus().toUpperCase());
                condition = condition.and(MEMBER.STATUS.eq(statusEnum));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
            }
        }

        // 초성 필터
        if (request.getNameInitial() != null && !request.getNameInitial().isBlank()) {
            condition = condition.and(MEMBER.EMPLOYEE_NAME.like(request.getNameInitial() + "%"));
        }

        // 정렬 필드 결정
        SortField<?> sortField;
        sortField = switch (sortBy) {
            case "employeeId" -> "desc".equalsIgnoreCase(sortDir)
                    ? MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.desc()
                    : MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.asc();
            case "joinedAt" -> "desc".equalsIgnoreCase(sortDir)
                    ? MEMBER.JOINED_AT.desc()
                    : MEMBER.JOINED_AT.asc();
            default -> "desc".equalsIgnoreCase(sortDir)
                    ? MEMBER.EMPLOYEE_NAME.desc()
                    : MEMBER.EMPLOYEE_NAME.asc();
        };

        // 총 개수 조회
        long total = dsl.selectCount()
                .from(MEMBER)
                .where(condition)
                .fetchOne(0, Long.class);

        // 기술스택 서브쿼리
        Table<?> topTechStack = dsl.select(
                        DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER,
                        DEVELOPER_TECH_STACK.TECH_STACK_NAME,
                        rowNumber().over()
                                .partitionBy(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER)
                                .orderBy(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES.desc())
                                .as("rn")
                )
                .from(DEVELOPER_TECH_STACK)
                .asTable("top_tech_stack");

        Field<String> topTechStackEmpId = topTechStack.field(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.getName(), String.class);
        Field<String> topTechStackName = topTechStack.field(DEVELOPER_TECH_STACK.TECH_STACK_NAME.getName(), String.class);
        Field<Integer> rowNumberField = topTechStack.field("rn", Integer.class);

        // 메인 쿼리
        List<MemberListResponse> content = dsl.select(
                        MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
                        MEMBER.EMPLOYEE_NAME,
                        MEMBER.PHONE_NUMBER,
                        MEMBER.EMAIL,
                        MEMBER.ROLE,
                        MEMBER.GRADE_CODE,
                        MEMBER.STATUS,
                        MEMBER.PROFILE_IMAGE_URL,
                        MEMBER.JOINED_AT,
                        topTechStackName,
                        MEMBER.CAREER_YEARS
                )
                .from(MEMBER)
                .leftJoin(topTechStack)
                .on(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(topTechStackEmpId)
                        .and(rowNumberField.eq(1)))
                .where(condition)
                .orderBy(sortField)
                .limit(size)
                .offset(page * size)
                .fetch(record -> new MemberListResponse(
                        record.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER),
                        record.get(MEMBER.EMPLOYEE_NAME),
                        record.get(MEMBER.PHONE_NUMBER),
                        record.get(MEMBER.EMAIL),
                        record.get(MEMBER.ROLE).name(),
                        record.get(MEMBER.GRADE_CODE) != null ? record.get(MEMBER.GRADE_CODE).name() : null,
                        record.get(MEMBER.STATUS) != null ? record.get(MEMBER.STATUS).name() : null,
                        record.get(MEMBER.PROFILE_IMAGE_URL),
                        record.get(MEMBER.JOINED_AT),
                        record.get(topTechStackName),
                        record.get(MEMBER.CAREER_YEARS)
                ));
        System.out.println("Status 파라미터: " + request.getStatus());
        return PageResponse.fromJooq(content, total, page, size);
    }
}