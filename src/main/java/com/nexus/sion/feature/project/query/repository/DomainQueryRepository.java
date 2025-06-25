package com.nexus.sion.feature.project.query.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.jooq.generated.tables.Domain.DOMAIN;

@Repository
@RequiredArgsConstructor
public class DomainQueryRepository {
    private final DSLContext dsl;

    public List<String> findAllDomains() {
        return dsl
                .select(DOMAIN.NAME)
                .from(DOMAIN)
                .fetchInto(String.class);
    }
}
