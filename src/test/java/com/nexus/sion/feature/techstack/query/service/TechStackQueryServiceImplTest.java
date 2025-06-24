package com.nexus.sion.feature.techstack.query.service;

import com.nexus.sion.feature.statistics.query.repository.StatisticsQueryRepository;
import com.nexus.sion.feature.statistics.query.service.StatisticsQueryServiceImpl;
import com.nexus.sion.feature.techstack.query.repository.TechStackQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechStackQueryServiceImplTest {
    @Mock
    private TechStackQueryRepository repository;

    @InjectMocks
    private TechStackQueryServiceImpl service;

    // 전체 기술 스택 이름 목록을 조회하는 기능을 테스트
    @Test
    void findAllStackNames_returnsList() {
        List<String> mockStacks = List.of("Java", "Spring", "Vue");
        when(repository.findAllStackNames()).thenReturn(mockStacks);

        List<String> result = service.findAllStackNames();

        assertEquals(3, result.size());
        assertTrue(result.contains("Spring"));
    }
}