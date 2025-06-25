package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.tables.Job.JOB;
import static com.nexus.sion.common.constants.CollationConstants.UTF8MB4_UNICODE_520_CI;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JobQueryRepository {
  private final DSLContext dsl;

  public List<String> findAllJobs() {
    return dsl.select(JOB.NAME)
        .from(JOB)
        // ㄱ-ㅎ, a-z 순서로 출력
        .orderBy(JOB.NAME.collate(UTF8MB4_UNICODE_520_CI).asc())
        .fetchInto(String.class);
  }
}
