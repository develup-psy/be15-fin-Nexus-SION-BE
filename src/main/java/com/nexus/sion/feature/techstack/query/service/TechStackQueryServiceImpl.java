package com.nexus.sion.feature.techstack.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.techstack.query.repository.TechStackQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TechStackQueryServiceImpl implements TechStackQueryService {

  private final TechStackQueryRepository repository;

  @Override
  public List<String> findAllStackNames() {
    return repository.findAllStackNames();
  }

  @Override
  public List<String> autocomplete(String keyword) {
    return repository.findAutoCompleteTechStacks(keyword);
  }
}
