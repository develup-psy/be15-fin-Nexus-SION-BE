package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.tables.Domain.DOMAIN;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DomainQueryRepository {
  private final DSLContext dsl;

  public List<String> findAllDomains() {
    return dsl
            .select(DOMAIN.NAME)
            .from(DOMAIN)
            .orderBy(DOMAIN.NAME.collate("utf8mb4_unicode_520_ci").asc())
            .fetchInto(String.class);
  }
}
