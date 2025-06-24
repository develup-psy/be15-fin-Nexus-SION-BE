package com.nexus.sion.feature.techstack.query.service;

import java.util.List;

import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.query.repository.TechStackQueryRepository;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.statistics.query.repository.StatisticsQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TechStackQueryServiceImpl implements TechStackQueryService {

  private final TechStackQueryRepository repository;

  @Override
  public List<String> findAllStackNames() {
    return repository.findAllStackNames();
  }
}