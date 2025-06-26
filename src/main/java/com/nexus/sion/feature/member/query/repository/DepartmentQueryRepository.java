package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.tables.Department.DEPARTMENT;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.member.query.dto.response.DepartmentResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DepartmentQueryRepository {

  private final DSLContext dsl;

  public List<DepartmentResponse> findAllDepartments() {
    return dsl.select(DEPARTMENT.DEPARTMENT_NAME)
        .from(DEPARTMENT)
        .fetchInto(DepartmentResponse.class);
  }
}
