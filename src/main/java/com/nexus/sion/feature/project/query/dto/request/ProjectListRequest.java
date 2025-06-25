package com.nexus.sion.feature.project.query.dto.request;

import java.util.List;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListRequest {
    private String keyword;
    private Long maxBudget;
    private Double maxPeriodInMonth;
    private Integer maxNumberOfMembers;
    private List<String> statuses;

    private int page;
    private int size;
}
