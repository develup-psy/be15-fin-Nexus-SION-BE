package com.nexus.sion.feature.techstack.command.application.service;

import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackRequest;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TechStackCommandServiceImplTest {

    @InjectMocks private TechStackCommandServiceImpl techStackCommandService;

    @Mock private TechStackRepository techStackRepository;

    @Mock private ModelMapper modelMapper;

    @Test
    void registerTechStack_이미존재하면저장하지않음() {
        // given
        TechStackRequest request = new TechStackRequest("Spring");
        when(techStackRepository.existsById("Spring")).thenReturn(true);

        // when
        techStackCommandService.registerTechStack(request);

        // then
        verify(techStackRepository, never()).save(any(TechStack.class));
    }

    @Test
    void registerTechStack_존재하지않으면저장() {
        // given
        TechStackRequest request = new TechStackRequest("React");

        when(techStackRepository.existsById("React")).thenReturn(false);
        when(modelMapper.map(request, TechStack.class)).thenReturn(mock(TechStack.class));

        // when
        techStackCommandService.registerTechStack(request);

        // then
        verify(techStackRepository, times(1)).save(any(TechStack.class));
    }

}