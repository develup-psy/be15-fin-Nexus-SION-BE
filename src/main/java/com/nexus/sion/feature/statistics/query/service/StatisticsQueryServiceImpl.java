package com.nexus.sion.feature.statistics.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.PopularTechStackDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCareerDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;
import com.nexus.sion.feature.statistics.query.repository.StatisticsQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsQueryServiceImpl implements StatisticsQueryService {

  private final StatisticsQueryRepository repository;

  @Override
  public List<TechStackCountDto> getStackMemberCounts(List<String> stackNames) {
    return repository.findStackMemberCount(stackNames);
  }

  @Override
  public List<String> findAllStackNames() {
    return repository.findAllStackNames();
  }

  @Override
  public PageResponse<DeveloperDto> getAllDevelopers(int page, int size) {
    return repository.findAllDevelopers(page, size);
  }

  @Override
  public PageResponse<TechStackCareerDto> getStackAverageCareersPaged(
      List<String> stackNames, int page, int size, String sort, String direction) {
    return repository.findStackAverageCareerPaged(stackNames, page, size, sort, direction);
  }

  @Override
  public PageResponse<PopularTechStackDto> getPopularTechStacks(String period, int page, int size) {
    return repository.findPopularTechStacks(period, page, size);
  }
}
