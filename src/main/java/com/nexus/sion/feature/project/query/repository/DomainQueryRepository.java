package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.tables.Domain.DOMAIN;
import static com.nexus.sion.common.constants.CollationConstants.UTF8MB4_UNICODE_520_CI;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DomainQueryRepository {
  private final DSLContext dsl;

  public List<String> findAllDomains() {
    return dsl.select(DOMAIN.NAME)
        .from(DOMAIN)
        .orderBy(DOMAIN.NAME.collate(UTF8MB4_UNICODE_520_CI).asc())
        .fetchInto(String.class);
  }
}
