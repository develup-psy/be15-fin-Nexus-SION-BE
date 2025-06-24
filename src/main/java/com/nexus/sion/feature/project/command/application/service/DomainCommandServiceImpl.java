package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;
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
  public boolean registerDomain(DomainRequest request) {
    // 기존에 존재하는 도메인은 저장하지 않고 종료
    if (domainRepository.existsById(request.getName())) {
      return false;
    }

    Domain domain = modelMapper.map(request, Domain.class);
    domainRepository.save(domain);
    return true;
  }

  @Override
  public void removeDomain(String domainName) {
    // 기존에 해당 도메인이 없으면 에러
    if(!domainRepository.existsById(domainName)) {
      throw new BusinessException(ErrorCode.DOMAIN_NOT_FOUND);
    }

    // 해당 도메인 삭제
    domainRepository.deleteById(domainName);
  }
}
