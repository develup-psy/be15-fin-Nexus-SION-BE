package com.nexus.sion.feature.project.command.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.DomainRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

@ExtendWith(MockitoExtension.class)
class DomainCommandServiceImplTest {
  @InjectMocks private DomainCommandServiceImpl domainCommandService;

  @Mock private DomainRepository domainRepository;

  @Mock private ModelMapper modelMapper;

  @Mock private ProjectRepository projectRepository;

  String domainName = "제조";

  @Test
  void registerDomain_이미존재하면저장하지않음() {
    // given
    DomainRequest request = DomainRequest.builder().name(domainName).build();
    when(domainRepository.existsById(domainName)).thenReturn(true);

    // when
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              domainCommandService.registerDomain(request);
            });

    // then
    assertEquals(ErrorCode.DOMAIN_ALREADY_EXIST, exception.getErrorCode());
    verify(domainRepository, never()).save(any(Domain.class));
  }

  @Test
  void registerDomain_존재하지않으면저장() {
    // given
    DomainRequest request = DomainRequest.builder().name(domainName).build();
    when(domainRepository.existsById(domainName)).thenReturn(false);
    when(modelMapper.map(request, Domain.class)).thenReturn(mock(Domain.class));

    // when
    domainCommandService.registerDomain(request);

    // then
    verify(domainRepository, times(1)).save(any(Domain.class));
  }

  @Test
  void deleteDomain_존재하면삭제() {
    // given
    when(domainRepository.existsById(domainName)).thenReturn(true);
    when(projectRepository.existsByDomainName(domainName)).thenReturn(false);
    doNothing().when(domainRepository).deleteById(domainName);

    // when
    domainCommandService.removeDomain(domainName);

    // then
    verify(domainRepository, times(1)).deleteById(domainName);
  }

  @Test
  void deleteDomain_존재하지않으면에러() {
    // given
    when(domainRepository.existsById(domainName)).thenReturn(false);

    // when & then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              domainCommandService.removeDomain(domainName);
            });

    // then
    assertEquals(ErrorCode.DOMAIN_NOT_FOUND, exception.getErrorCode());

    verify(domainRepository, never()).deleteById(any());
  }

  @Test
  void removeDomain_연결된_프로젝트가_있으면_예외발생() {
    // given
    when(domainRepository.existsById(domainName)).thenReturn(true);
    when(projectRepository.existsByDomainName(domainName)).thenReturn(true);

    // when & then
    BusinessException exception =
            assertThrows(
                    BusinessException.class,
                    () -> domainCommandService.removeDomain(domainName));

    assertEquals(ErrorCode.DOMAIN_DELETE_CONSTRAINT, exception.getErrorCode());
  }
}
