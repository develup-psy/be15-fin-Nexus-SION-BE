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
  public PageResponse<DeveloperDto> getAllDevelopers(int page, int size) {
    return statisticsQueryRepository.findAllDevelopers(page, size);
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
  public PageResponse<TechAdoptionTrendDto> getTechAdoptionTrendsByYearPaged(
      int year, int page, int size) {
    List<TechAdoptionTrendDto> all = statisticsQueryRepository.findTechAdoptionTrendsByYear(year);

    int total = all.size();
    int fromIndex = page * size;
    int toIndex = Math.min(fromIndex + size, total);

    List<TechAdoptionTrendDto> pageContent =
        fromIndex >= total ? List.of() : all.subList(fromIndex, toIndex);

    return PageResponse.fromJooq(pageContent, total, page, size);
  }

  @Override
  public List<Integer> getProjectYears() {
    return statisticsQueryRepository.findProjectYears();
  }
}
