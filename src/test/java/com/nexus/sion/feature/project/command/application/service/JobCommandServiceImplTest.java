package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.JobRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Job;
import com.nexus.sion.feature.project.command.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobCommandServiceImplTest {
    @InjectMocks private JobCommandServiceImpl jobCommandService;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ModelMapper modelMapper;

    String jobName = "백엔드";


    @Test
    void registerDomain_이미존재하면저장하지않음() {
        // given
        JobRequest request = JobRequest.builder().name(jobName).build();
        when(jobRepository.existsById(jobName)).thenReturn(true);

        // when
        boolean result = jobCommandService.registerJob(request);

        // then
        assertFalse(result); // 반환값이 false인지 검증
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void registerDomain_존재하지않으면저장() {
        // given
        JobRequest request = JobRequest.builder().name(jobName).build();
        when(jobRepository.existsById(jobName)).thenReturn(false);
        when(modelMapper.map(request, Job.class)).thenReturn(mock(Job.class));

        // when
        boolean result = jobCommandService.registerJob(request);

        // then
        assertTrue(result); // 반환값이 false인지 검증
        verify(jobRepository, times(1)).save(any(Job.class));
    }

}