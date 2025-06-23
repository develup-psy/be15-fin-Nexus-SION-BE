package com.nexus.sion.feature.statistics.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCareerDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;
import com.nexus.sion.feature.statistics.query.repository.StatisticsQueryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StatisticsQueryServiceImplTest {

    @Mock
    private StatisticsQueryRepository repository;

    @InjectMocks
    private StatisticsQueryServiceImpl service;

    private final List<String> sampleStacks = List.of("Java", "React");

    // 기술 스택별 구성원 수를 조회하는 기능을 테스트
    @Test
    void getStackMemberCounts_returnsDtoList() {
        List<TechStackCountDto> mockResult = List.of(
                new TechStackCountDto("Java", 3),
                new TechStackCountDto("React", 5)
        );
        when(repository.findStackMemberCount(sampleStacks)).thenReturn(mockResult);

        List<TechStackCountDto> result = service.getStackMemberCounts(sampleStacks);

        assertEquals(2, result.size());
        assertEquals("Java", result.get(0).getTechStackName());
        assertEquals(5, result.get(1).getCount());
    }

    // 전체 기술 스택 이름 목록을 조회하는 기능을 테스트
    @Test
    void findAllStackNames_returnsList() {
        List<String> mockStacks = List.of("Java", "Spring", "Vue");
        when(repository.findAllStackNames()).thenReturn(mockStacks);

        List<String> result = service.findAllStackNames();

        assertEquals(3, result.size());
        assertTrue(result.contains("Spring"));
    }

    // 전체 개발자 목록을 페이지 단위로 조회하는 기능을 테스트
    @Test
    void getAllDevelopers_returnsPageResponse() {
        PageResponse<DeveloperDto> mockPage = PageResponse.fromJooq(
                List.of(new DeveloperDto()), 1, 0, 10
        );
        when(repository.findAllDevelopers(0, 10)).thenReturn(mockPage);

        PageResponse<DeveloperDto> result = service.getAllDevelopers(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    // 기술 스택별 평균 경력 통계를 페이지 단위로 조회하는 기능을 테스트
    @Test
    void getStackAverageCareersPaged_returnsPagedCareerStats() {
        PageResponse<TechStackCareerDto> mockPage = PageResponse.fromJooq(
                List.of(new TechStackCareerDto("Java", 5.2, 1.0, 10.0, 3)),
                1, 0, 10
        );
        when(repository.findStackAverageCareerPaged(sampleStacks, 0, 10, "averageCareer", "desc"))
                .thenReturn(mockPage);

        PageResponse<TechStackCareerDto> result = service.getStackAverageCareersPaged(
                sampleStacks, 0, 10, "averageCareer", "desc"
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Java", result.getContent().get(0).getTechStackName());
    }
}
