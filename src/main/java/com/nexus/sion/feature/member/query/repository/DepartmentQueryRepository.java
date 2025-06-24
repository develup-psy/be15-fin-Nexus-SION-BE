package com.nexus.sion.feature.member.query.repository;

import com.nexus.sion.feature.member.query.dto.response.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.jooq.generated.tables.Department.DEPARTMENT;

@Repository
@RequiredArgsConstructor
public class DepartmentQueryRepository {

    private final DSLContext dsl;

    public List<DepartmentResponse> findAllDepartments() {
        return dsl.select(
                        DEPARTMENT.DEPARTMENT_NAME,
                        DEPARTMENT.CREATE_AT,
                        DEPARTMENT.UPDATED_AT
                )
                .from(DEPARTMENT)
                .fetchInto(DepartmentResponse.class);
    }
}
