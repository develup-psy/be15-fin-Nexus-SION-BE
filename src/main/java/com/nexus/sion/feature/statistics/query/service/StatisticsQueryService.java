package com.nexus.sion.feature.statistics.query.service;

import java.util.List;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.*;

public interface StatisticsQueryService {
  List<TechStackCountDto> getStackMemberCounts(List<String> stackNames);

  PageResponse<DeveloperDto> getAllDevelopers(int page, int size);

  PageResponse<TechStackCareerDto> getStackAverageCareersPaged(
      List<String> stackNames, int page, int size, String sort, String direction);

  PageResponse<TechStackMonthlyUsageDto> getPopularTechStacksGroupedByMonth(
      String period, int page, int size, Integer top);

  List<JobParticipationStatsDto> getJobParticipationStats();
}
