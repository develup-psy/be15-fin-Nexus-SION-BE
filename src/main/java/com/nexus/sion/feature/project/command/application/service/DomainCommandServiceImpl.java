package com.nexus.sion.feature.project.command.application.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.command.application.dto.request.DomainRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DomainCommandServiceImpl implements DomainCommandService {

  private final ModelMapper modelMapper;
  private final DomainRepository domainRepository;

  @Override
  public void registerDomain(DomainRequest request) {
    // 기존에 존재하는 도메인은 저장하지 않고 종료
    if (domainRepository.existsById(request.getName())) {
      return;
    }

    Domain domain = modelMapper.map(request, Domain.class);
    domainRepository.save(domain);
  }
}
