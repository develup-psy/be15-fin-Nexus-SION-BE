package com.nexus.sion.feature.project.command.application.service;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.DomainRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DomainCommandServiceImpl implements DomainCommandService {

  private final ModelMapper modelMapper;
  private final DomainRepository domainRepository;
  private final ProjectRepository projectRepository;

  @Override
  @Transactional
  public void registerDomain(DomainRequest request) {
    // 기존에 존재하는 도메인은 에러
    if (domainRepository.existsById(request.getName())) {
      throw new BusinessException(ErrorCode.DOMAIN_ALREADY_EXIST);
    }

    Domain domain = modelMapper.map(request, Domain.class);
    domainRepository.save(domain);
  }

  @Override
  @Transactional
  public void removeDomain(String domainName) {
    // 기존에 해당 도메인이 없으면 에러
    if (!domainRepository.existsById(domainName)) {
      throw new BusinessException(ErrorCode.DOMAIN_NOT_FOUND);
    }

    // 도메인을 사용하는 프로젝트가 있는지 확인
    if (projectRepository.existsByDomainName(domainName)) {
      throw new BusinessException(ErrorCode.DOMAIN_DELETE_CONSTRAINT);
    }

    // 해당 도메인 삭제
    domainRepository.deleteById(domainName);
  }
}
