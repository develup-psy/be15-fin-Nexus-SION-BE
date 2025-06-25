package com.nexus.sion.feature.project.query.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.jooq.generated.tables.Job.JOB;

@Repository
@RequiredArgsConstructor
public class JobQueryRepository {
    private final DSLContext dsl;

    public List<String> findAllJobs() {
        return dsl
                .select(JOB.NAME)
                .from(JOB)
                // ㄱ-ㅎ, a-z 순서로 출력
                .orderBy(JOB.NAME.collate("utf8mb4_unicode_520_ci").asc())
                .fetchInto(String.class);
    }
}
