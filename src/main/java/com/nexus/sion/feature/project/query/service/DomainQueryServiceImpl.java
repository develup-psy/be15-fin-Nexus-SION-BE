package com.nexus.sion.feature.project.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.query.repository.DomainQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DomainQueryServiceImpl implements DomainQueryService {

  private final DomainQueryRepository domainQueryRepository;

  @Override
  public List<String> findAllDomains() {
    return domainQueryRepository.findAllDomains();
  }
}
