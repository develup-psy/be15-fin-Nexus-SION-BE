package com.nexus.sion.feature.project.query.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexus.sion.feature.project.query.repository.JobQueryRepository;

@ExtendWith(MockitoExtension.class)
class JobQueryServiceImplTest {
  @Mock private JobQueryRepository jobQueryRepository;

  @InjectMocks private JobQueryServiceImpl jobQueryService;

  // 전체 직무 목록을 조회하는 기능을 테스트
  @Test
  void findAllJobNames_returnList() {
    List<String> mockJobs = List.of("백엔드", "프론트엔드", "데브옵스엔지니어");
    when(jobQueryRepository.findAllJobs()).thenReturn(mockJobs);

    List<String> result = jobQueryService.findAllJobs();

    assertEquals(3, result.size());
    assertTrue(result.contains("백엔드"));
  }
}
