package com.nexus.sion.feature.statistics.query.repository;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;

import java.util.List;

public interface StatisticsQueryRepository {
    List<TechStackCountDto> findStackMemberCount(List<String> techStackNames);
    List<String> findAllStackNames();
    PageResponse<DeveloperDto> findAllDevelopers(int page, int size);
}
