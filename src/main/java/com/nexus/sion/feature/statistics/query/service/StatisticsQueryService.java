package com.nexus.sion.feature.statistics.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCareerDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;

import java.util.List;

public interface StatisticsQueryService {
  List<TechStackCountDto> getStackMemberCounts(List<String> stackNames);
  List<String> findAllStackNames();
  PageResponse<DeveloperDto> getAllDevelopers(int page, int size);
  PageResponse<TechStackCareerDto> getStackAverageCareersPaged(
          List<String> stackNames, int page, int size, String sort, String direction);
}
