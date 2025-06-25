package com.nexus.sion.feature.project.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.query.repository.JobQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobQueryServiceImpl implements JobQueryService {

  private final JobQueryRepository jobQueryRepository;

  @Override
  public List<String> findAllJobs() {
    return jobQueryRepository.findAllJobs();
  }
}
