package com.nexus.sion.feature.techstack.command.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackRequest;
import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;

@ExtendWith(MockitoExtension.class)
class TechStackCommandServiceImplTest {

  @InjectMocks private TechStackCommandServiceImpl techStackCommandService;

  @Mock private TechStackRepository techStackRepository;

  @Mock private ModelMapper modelMapper;

  String techStackName = "Java";

  @Test
  void registerTechStack_이미존재하면저장하지않음() {
    // given
    TechStackRequest request = new TechStackRequest(techStackName);
    when(techStackRepository.existsById(techStackName)).thenReturn(true);

    // when
    BusinessException exception =
            assertThrows(
                    BusinessException.class,
                    () -> {
                      techStackCommandService.registerTechStack(request);
                    });

    // then
    assertEquals(ErrorCode.TECH_STACK_ALREADY_EXIST, exception.getErrorCode());
    verify(techStackRepository, never()).save(any(TechStack.class));
  }

  @Test
  void registerTechStack_존재하지않으면저장() {
    // given
    TechStackRequest request = new TechStackRequest(techStackName);

    when(techStackRepository.existsById(techStackName)).thenReturn(false);
    when(modelMapper.map(request, TechStack.class)).thenReturn(mock(TechStack.class));

    // when
    techStackCommandService.registerTechStack(request);

    // then
    verify(techStackRepository, times(1)).save(any(TechStack.class));
  }

  @Test
  void deleteTechStack_존재하면삭제() {
    // given
    when(techStackRepository.existsById(techStackName)).thenReturn(true);
    doNothing().when(techStackRepository).deleteById(techStackName);

    // when
    techStackCommandService.removeTechStack(techStackName);

    // then
    verify(techStackRepository, times(1)).deleteById(techStackName);
  }

  @Test
  void deleteTechStack_존재하지않으면에러() {
    // given
    when(techStackRepository.existsById(techStackName)).thenReturn(false);

    // when & then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              techStackCommandService.removeTechStack(techStackName);
            });

    // then
    assertEquals(ErrorCode.TECH_STACK_NOT_FOUND, exception.getErrorCode());

    verify(techStackRepository, never()).deleteById(any());
  }
}
