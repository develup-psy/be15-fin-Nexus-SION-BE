package com.nexus.sion.feature.techstack.query.repository;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.TechStack.TECH_STACK;

import java.util.*;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TechStackQueryRepository {

  private final DSLContext dsl;



  public List<String> findAllStackNames() {
    return dsl.select(TECH_STACK.TECH_STACK_NAME).from(TECH_STACK).fetchInto(String.class);
  }
}