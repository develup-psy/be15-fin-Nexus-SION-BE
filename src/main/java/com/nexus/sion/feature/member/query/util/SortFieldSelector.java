package com.nexus.sion.feature.member.query.util;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import org.jooq.SortField;
import org.springframework.stereotype.Component;

import static com.example.jooq.generated.tables.Grade.GRADE;
import static com.example.jooq.generated.tables.Member.MEMBER;

@Component
public class SortFieldSelector {

    public SortField<?> select(String sortBy, String sortDir) {
        boolean isDesc = "desc".equalsIgnoreCase(sortDir);

        return switch (sortBy) {
            case "employeeId" -> isDesc
                    ? MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.desc()
                    : MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.asc();
            case "joinedAt" -> isDesc
                    ? MEMBER.JOINED_AT.desc()
                    : MEMBER.JOINED_AT.asc();
            case "grade" -> isDesc
                    ? MEMBER.GRADE_CODE.desc()
                    : MEMBER.GRADE_CODE.asc();
            case "employeeName" -> isDesc
                    ? MEMBER.EMPLOYEE_NAME.desc()
                    : MEMBER.EMPLOYEE_NAME.asc();
            default -> throw new BusinessException(ErrorCode.INVALID_SORT_COLUMN);
        };
    }
}

