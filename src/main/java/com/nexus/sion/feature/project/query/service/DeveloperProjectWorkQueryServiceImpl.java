package com.nexus.sion.feature.project.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.query.dto.response.WorkRequestQueryDto;
import com.nexus.sion.feature.project.query.repository.DeveloperProjectWorkQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeveloperProjectWorkQueryServiceImpl implements DeveloperProjectWorkQueryService {

  private final DeveloperProjectWorkQueryRepository repository;

  @Override
  public List<WorkRequestQueryDto> getAllRequests() {
    return repository.findAll();
  }
}
