package com.nexus.sion.feature.member.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import com.nexus.sion.feature.member.query.dto.response.CertificateResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateQueryServiceImpl implements CertificateQueryService {

  private final CertificateRepository certificateRepository;

  @Override
  public List<CertificateResponse> getAllCertificates() {
    return certificateRepository.findAllAsResponse();
  }
}
