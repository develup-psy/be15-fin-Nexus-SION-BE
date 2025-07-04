package com.nexus.sion.feature.member.query.util;

import static com.example.jooq.generated.tables.Member.MEMBER;

import org.jooq.SortField;
import org.springframework.stereotype.Component;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;

@Component
public class SortFieldSelector {

  public SortField<?> select(String sortBy, String sortDir) {
    boolean isDesc = "desc".equalsIgnoreCase(sortDir);

    return switch (sortBy) {
      case "grade" -> isDesc ? MEMBER.GRADE_CODE.desc() : MEMBER.GRADE_CODE.asc();
      case "employeeName" -> isDesc ? MEMBER.EMPLOYEE_NAME.desc() : MEMBER.EMPLOYEE_NAME.asc();
      default -> throw new BusinessException(ErrorCode.INVALID_SORT_COLUMN);
    };
  }
}
