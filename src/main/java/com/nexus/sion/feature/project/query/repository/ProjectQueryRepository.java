package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.tables.Project.PROJECT;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ProjectListRequest;
import com.nexus.sion.feature.project.query.dto.response.ProjectListResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {

    private final DSLContext dsl;

    public PageResponse<ProjectListResponse> findProjects(ProjectListRequest request) {

        // 1. 키워드 조건 (내부 OR)
        Condition keywordCondition = DSL.noCondition();
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = "%" + request.getKeyword() + "%";
            keywordCondition = PROJECT.TITLE.likeIgnoreCase(keyword)
                    .or(PROJECT.DOMAIN_NAME.likeIgnoreCase(keyword))
                    .or(PROJECT.DESCRIPTION.likeIgnoreCase(keyword));
        }

        // 2. 나머지 필터 조건 (AND)
        Condition filterCondition = PROJECT.DELETED_AT.isNull();
        if (request.getMaxBudget() != null) {
            filterCondition = filterCondition.and(PROJECT.BUDGET.le(request.getMaxBudget()));
        }
        if (request.getMaxNumberOfMembers() != null) {
            filterCondition = filterCondition.and(PROJECT.NUMBER_OF_MEMBERS.le(request.getMaxNumberOfMembers()));
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            filterCondition = filterCondition.and(PROJECT.STATUS.in(request.getStatuses()));
        }

        // 최종 조건: 키워드 AND 필터 조건
        Condition finalCondition = keywordCondition.and(filterCondition);

        // 전체 개수
        long totalCount = dsl.selectCount().from(PROJECT).where(finalCondition).fetchOne(0, long.class);

        // 데이터 조회 (페이징 적용)
        List<ProjectListResponse> content = dsl.selectFrom(PROJECT)
                .where(finalCondition)
                .orderBy(PROJECT.CREATED_AT.desc())
                .limit(request.getSize())
                .offset(request.getPage() * request.getSize())
                .fetch()
                .stream()
                .filter(record -> {
                    if (request.getMaxPeriodInMonth() == null) return true;
                    LocalDate start = record.get(PROJECT.START_DATE);
                    LocalDate end = record.get(PROJECT.ACTUAL_END_DATE) != null
                            ? record.get(PROJECT.ACTUAL_END_DATE)
                            : record.get(PROJECT.EXPECTED_END_DATE);
                    long months = ChronoUnit.MONTHS.between(start, end);
                    return months <= request.getMaxPeriodInMonth();
                })
                .map(record -> {
                    LocalDate start = record.get(PROJECT.START_DATE);
                    LocalDate end = record.get(PROJECT.ACTUAL_END_DATE) != null
                            ? record.get(PROJECT.ACTUAL_END_DATE)
                            : record.get(PROJECT.EXPECTED_END_DATE);
                    String period = start + " ~ " + end;

                    return new ProjectListResponse(
                            record.get(PROJECT.PROJECT_CODE),
                            record.get(PROJECT.TITLE),
                            record.get(PROJECT.DESCRIPTION),
                            period,
                            String.valueOf(record.get(PROJECT.STATUS)),
                            record.get(PROJECT.DOMAIN_NAME),
                            record.get(PROJECT.NUMBER_OF_MEMBERS)
                    );
                })
                .collect(Collectors.toList());

        return PageResponse.fromJooq(content, totalCount, request.getPage(), request.getSize());
    }
}
