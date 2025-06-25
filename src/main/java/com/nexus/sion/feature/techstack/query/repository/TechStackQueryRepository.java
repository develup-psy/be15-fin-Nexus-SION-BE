package com.nexus.sion.feature.techstack.query.repository;

import static com.example.jooq.generated.tables.TechStack.TECH_STACK;

import java.util.*;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TechStackQueryRepository {

  private final DSLContext dsl;

  public List<String> findAllStackNames() {
    return dsl
            .select(TECH_STACK.TECH_STACK_NAME)
            .from(TECH_STACK)
            .orderBy(TECH_STACK.TECH_STACK_NAME.collate("utf8mb4_unicode_520_ci").asc())
            .fetchInto(String.class);
  }
}
