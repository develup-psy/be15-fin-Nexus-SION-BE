package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.feature.project.query.repository.DomainQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainQueryServiceImplTest {

    @Mock private DomainQueryRepository domainQueryRepository;

    @InjectMocks private DomainQueryServiceImpl domainQueryService;

    // 전체 도메인 이름 목록을 조회하는 기능을 테스트
    @Test
    void findAllDomainNames_returnList() {
        List<String> mockDomains = List.of("제조", "유통", "코스메틱");
        when(domainQueryRepository.findAllDomains()).thenReturn(mockDomains);

        List<String> result = domainQueryService.findAllDomains();

        assertEquals(3, result.size());
        assertTrue(result.contains("제조"));
    }
}