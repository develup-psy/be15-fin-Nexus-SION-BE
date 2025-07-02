package com.nexus.sion.feature.techstack.query.repository;

import static com.example.jooq.generated.tables.TechStack.TECH_STACK;
import static com.nexus.sion.common.constants.CollationConstants.UTF8MB4_UNICODE_520_CI;

import java.util.*;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TechStackQueryRepository {

  private final DSLContext dsl;

  public List<String> findAllStackNames() {
    return dsl.select(TECH_STACK.TECH_STACK_NAME)
        .from(TECH_STACK)
        .orderBy(TECH_STACK.TECH_STACK_NAME.collate(UTF8MB4_UNICODE_520_CI).asc())
        .fetchInto(String.class);
  }

  public List<String> findAutoCompleteTechStacks(String keyword) {
    Field<String> nameField = TECH_STACK.TECH_STACK_NAME;

    // CASE 구문으로 시작하는 경우 우선순위 부여
    Field<Integer> orderPriority =
            DSL.when(nameField.likeIgnoreCase(keyword + "%"), 0)
                    .otherwise(1);

    return dsl.select(nameField)
            .from(TECH_STACK)
            .where(nameField.containsIgnoreCase(keyword))
            .orderBy(orderPriority.asc(), nameField.asc())
            .limit(5)
            .fetchInto(String.class);
  }
}
