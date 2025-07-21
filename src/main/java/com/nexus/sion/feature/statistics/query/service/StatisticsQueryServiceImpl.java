package com.nexus.sion.feature.statistics.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.*;
import com.nexus.sion.feature.statistics.query.repository.StatisticsQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsQueryServiceImpl implements StatisticsQueryService {

  private final StatisticsQueryRepository statisticsQueryRepository;

  @Override
  public List<TechStackCountDto> getStackMemberCounts(List<String> stackNames) {
    return statisticsQueryRepository.findStackMemberCount(stackNames);
  }

  @Override
  public PageResponse<DeveloperDto> getDevelopersByStack(int page, int size, List<String> stackFilters) {
    return statisticsQueryRepository.findDevelopersByStack(page, size, stackFilters);
  }

  @Override
  public PageResponse<TechStackCareerDto> getStackAverageCareersPaged(
      List<String> stackNames, int page, int size, String sort, String direction) {
    return statisticsQueryRepository.findStackAverageCareerPaged(
        stackNames, page, size, sort, direction);
  }

  @Override
  public PageResponse<TechStackMonthlyUsageDto> getPopularTechStacksGroupedByMonth(
      String period, int page, int size, Integer top) {
    return statisticsQueryRepository.findMonthlyPopularTechStacks(period, page, size, top);
  }

  @Override
  public List<JobParticipationStatsDto> getJobParticipationStats() {
    return statisticsQueryRepository.getJobParticipationStats();
  }

  @Override
  public List<MemberWaitingCountDto> getWaitingCountsByGrade() {
    return statisticsQueryRepository.findWaitingCountByGrade();
  }

  @Override
  public List<GradeSalaryStatsDto> getGradeSalaryStats() {
    return statisticsQueryRepository.getGradeSalaryStatistics();
  }

  @Override
  public List<TechAdoptionTrendDto> getTechAdoptionTrendsByYear(int year) {
    return statisticsQueryRepository.findTechAdoptionTrendsByYear(year);
  }

  @Override
  public List<Integer> getProjectYears() {
    return statisticsQueryRepository.findProjectYears();
  }
}
