package com.nexus.sion.feature.project.command.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.nexus.sion.feature.project.command.application.dto.request.DomainRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

@ExtendWith(MockitoExtension.class)
class DomainCommandServiceImplTest {
  @InjectMocks private DomainCommandServiceImpl domainCommandService;

  @Mock private DomainRepository domainRepository;

  @Mock private ModelMapper modelMapper;

  String domainName = "제조";

  @Test
  void registerDomain_이미존재하면저장하지않음() {
    // given
    DomainRequest request = new DomainRequest(domainName);
    when(domainRepository.existsById(domainName)).thenReturn(true);

    // when
    boolean result = domainCommandService.registerDomain(request);

    // then
    assertFalse(result); // 반환값이 false인지 검증
    verify(domainRepository, never()).save(any(Domain.class));
  }

  @Test
  void registerDomain_존재하지않으면저장() {
    // given
    DomainRequest request = new DomainRequest(domainName);
    when(domainRepository.existsById(domainName)).thenReturn(false);
    when(modelMapper.map(request, Domain.class)).thenReturn(mock(Domain.class));

    // when
    boolean result = domainCommandService.registerDomain(request);

    // then
    assertTrue(result); // 반환값이 false인지 검증
    verify(domainRepository, times(1)).save(any(Domain.class));
  }
}
