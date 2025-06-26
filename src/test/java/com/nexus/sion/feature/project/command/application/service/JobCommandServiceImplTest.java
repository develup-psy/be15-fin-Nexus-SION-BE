package com.nexus.sion.feature.project.command.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.JobRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Job;
import com.nexus.sion.feature.project.command.repository.JobRepository;

@ExtendWith(MockitoExtension.class)
class JobCommandServiceImplTest {
  @InjectMocks private JobCommandServiceImpl jobCommandService;

  @Mock private JobRepository jobRepository;

  @Mock private ModelMapper modelMapper;

  String jobName = "백엔드";

  @Test
  void registerJob_이미존재하면저장하지않음() {
    // given
    JobRequest request = JobRequest.builder().name(jobName).build();
    when(jobRepository.existsById(jobName)).thenReturn(true);

    // when
    BusinessException exception =
            assertThrows(
                    BusinessException.class,
                    () -> {
                      jobCommandService.registerJob(request);
                    });

    // then
    assertEquals(ErrorCode.JOB_ALREADY_EXIST, exception.getErrorCode());
    verify(jobRepository, never()).save(any(Job.class));
  }

  @Test
  void registerJob_존재하지않으면저장() {
    // given
    JobRequest request = JobRequest.builder().name(jobName).build();
    when(jobRepository.existsById(jobName)).thenReturn(false);
    when(modelMapper.map(request, Job.class)).thenReturn(mock(Job.class));

    // when
    jobCommandService.registerJob(request);

    // then
    verify(jobRepository, times(1)).save(any(Job.class));
  }

  @Test
  void deleteJob_존재하면삭제() {
    // given
    when(jobRepository.existsById(jobName)).thenReturn(true);
    doNothing().when(jobRepository).deleteById(jobName);

    // when
    jobCommandService.removeJob(jobName);

    // then
    verify(jobRepository, times(1)).deleteById(jobName);
  }

  @Test
  void deleteJob_존재하지않으면에러() {
    // given
    when(jobRepository.existsById(jobName)).thenReturn(false);

    // when & then
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              jobCommandService.removeJob(jobName);
            });

    // then
    assertEquals(ErrorCode.JOB_NOT_FOUND, exception.getErrorCode());

    verify(jobRepository, never()).deleteById(any());
  }
}
