package com.nexus.sion.feature.statistics.query.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.*;
import com.nexus.sion.feature.statistics.query.repository.StatisticsQueryRepository;

@ExtendWith(MockitoExtension.class)
class StatisticsQueryServiceImplTest {

  @Mock private StatisticsQueryRepository repository;

  @InjectMocks private StatisticsQueryServiceImpl service;

  private final List<String> sampleStacks = List.of("Java", "React");

  // 기술 스택별 구성원 수를 조회하는 기능을 테스트
  @Test
  void getStackMemberCounts_returnsDtoList() {
    List<TechStackCountDto> mockResult =
        List.of(new TechStackCountDto("Java", 3), new TechStackCountDto("React", 5));
    when(repository.findStackMemberCount(sampleStacks)).thenReturn(mockResult);

    List<TechStackCountDto> result = service.getStackMemberCounts(sampleStacks);

    assertEquals(2, result.size());
    assertEquals("Java", result.get(0).getTechStackName());
    assertEquals(5, result.get(1).getCount());
  }

  // 전체 개발자 목록을 페이지 단위로 조회하는 기능을 테스트
  @Test
  void getAllDevelopers_returnsPageResponse() {
    PageResponse<DeveloperDto> mockPage =
        PageResponse.fromJooq(List.of(new DeveloperDto()), 1, 0, 10);
    when(repository.findAllDevelopers(0, 10)).thenReturn(mockPage);

    PageResponse<DeveloperDto> result = service.getAllDevelopers(0, 10);

    assertEquals(1, result.getTotalElements());
  }

  // 기술 스택별 평균 경력 통계를 페이지 단위로 조회하는 기능을 테스트
  @Test
  void getStackAverageCareersPaged_returnsPagedCareerStats() {
    PageResponse<TechStackCareerDto> mockPage =
        PageResponse.fromJooq(List.of(new TechStackCareerDto("Java", 5.2, 1.0, 10.0, 3)), 1, 0, 10);
    when(repository.findStackAverageCareerPaged(sampleStacks, 0, 10, "averageCareer", "desc"))
        .thenReturn(mockPage);

    PageResponse<TechStackCareerDto> result =
        service.getStackAverageCareersPaged(sampleStacks, 0, 10, "averageCareer", "desc");

    assertEquals(1, result.getTotalElements());
    assertEquals("Java", result.getContent().get(0).getTechStackName());
  }

  // 기간별 인기 기술 스택 통계를 조회하는 기능을 테스트
  @Test
  void getPopularTechStacksGroupedByMonth_returnsMonthlyUsageStats() {
    String period = "6m";
    int page = 0;
    int size = 10;
    int top = 5;

    TechStackMonthlyUsageDto dto1 =
        TechStackMonthlyUsageDto.builder()
            .techStackName("Spring Boot")
            .monthlyUsage(Map.of("2025-01", 12, "2025-02", 8))
            .totalUsageCount(20)
            .latestProjectName("프로젝트A")
            .topJobName("백엔드")
            .build();

    TechStackMonthlyUsageDto dto2 =
        TechStackMonthlyUsageDto.builder()
            .techStackName("React")
            .monthlyUsage(Map.of("2025-01", 5, "2025-02", 9))
            .totalUsageCount(14)
            .latestProjectName("프로젝트B")
            .topJobName("프론트엔드")
            .build();

    PageResponse<TechStackMonthlyUsageDto> mockPage =
        PageResponse.fromJooq(List.of(dto1, dto2), 2, page, size);

    when(repository.findMonthlyPopularTechStacks(period, page, size, top)).thenReturn(mockPage);

    PageResponse<TechStackMonthlyUsageDto> result =
        service.getPopularTechStacksGroupedByMonth(period, page, size, top);

    assertEquals(2, result.getTotalElements());
    assertEquals("Spring Boot", result.getContent().get(0).getTechStackName());
    assertEquals(20, result.getContent().get(0).getTotalUsageCount());
    assertEquals(9, result.getContent().get(1).getMonthlyUsage().get("2025-02"));
  }
}
