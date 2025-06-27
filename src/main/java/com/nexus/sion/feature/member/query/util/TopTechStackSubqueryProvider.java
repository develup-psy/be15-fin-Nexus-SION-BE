package com.nexus.sion.feature.member.query.util;

import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.springframework.stereotype.Component;

import static com.example.jooq.generated.Tables.DEVELOPER_TECH_STACK;
import static org.jooq.impl.DSL.rowNumber;

@Component
@RequiredArgsConstructor
public class TopTechStackSubqueryProvider {

    private final DSLContext dsl;

    public TopTechStackSubquery getTopTechStackSubquery() {
        Table<?> topTechStack = dsl.select(
                        DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER,
                        DEVELOPER_TECH_STACK.TECH_STACK_NAME,
                        rowNumber().over()
                                .partitionBy(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER)
                                .orderBy(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES.desc())
                                .as("rn"))
                .from(DEVELOPER_TECH_STACK)
                .asTable("top_tech_stack");

        return new TopTechStackSubquery(
                topTechStack,
                topTechStack.field(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER.getName(), String.class),
                topTechStack.field(DEVELOPER_TECH_STACK.TECH_STACK_NAME.getName(), String.class),
                topTechStack.field("rn", Integer.class)
        );
    }

    public record TopTechStackSubquery(
            Table<?> table,
            Field<String> empId,
            Field<String> techStackName,
            Field<Integer> rowNumberField
    ) {}
}

