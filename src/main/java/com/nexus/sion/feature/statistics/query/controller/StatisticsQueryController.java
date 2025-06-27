package com.nexus.sion.feature.statistics.query.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.*;
import com.nexus.sion.feature.statistics.query.service.StatisticsQueryService;
import com.nexus.sion.feature.techstack.query.service.TechStackQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
public class StatisticsQueryController {

  private final StatisticsQueryService statisticsQueryService;
  private final TechStackQueryService techStackQueryService;

  @PostMapping("/stack/member-count")
  public ResponseEntity<ApiResponse<List<TechStackCountDto>>> getStackCount(
      @RequestBody List<String> stacks) {
    var response = statisticsQueryService.getStackMemberCounts(stacks);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/all-tech-stacks")
  public ResponseEntity<ApiResponse<List<String>>> getAllTechStacks() {
    var response = techStackQueryService.findAllStackNames();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/developers")
  public ResponseEntity<ApiResponse<PageResponse<DeveloperDto>>> getAllDevelopers(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
    var response = statisticsQueryService.getAllDevelopers(page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/stack/average-career")
  public ResponseEntity<ApiResponse<PageResponse<TechStackCareerDto>>> getStackAverageCareerPaged(
      @RequestParam List<String> selectedStacks,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "techStackName") String sort,
      @RequestParam(defaultValue = "asc") String direction) {
    var response =
        statisticsQueryService.getStackAverageCareersPaged(
            selectedStacks, page, size, sort, direction);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/stack/popular")
  public ResponseEntity<ApiResponse<PageResponse<TechStackMonthlyUsageDto>>>
      getMonthlyPopularTechStacks(
          @RequestParam String period,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(name = "top", required = false) Integer top) {
    PageResponse<TechStackMonthlyUsageDto> response;

    if (top != null) {
      // top이 지정된 경우: page=0, size=top 고정, total도 top으로 제한
      response = statisticsQueryService.getPopularTechStacksGroupedByMonth(period, 0, top, top);
      long totalElements = Math.min(top, response.getContent().size());
      PageResponse<TechStackMonthlyUsageDto> trimmed =
          PageResponse.fromJooq(response.getContent(), totalElements, 0, top);
      return ResponseEntity.ok(ApiResponse.success(trimmed));
    }

    // 일반 페이징
    response = statisticsQueryService.getPopularTechStacksGroupedByMonth(period, page, size, null);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/participation")
  public ResponseEntity<ApiResponse<List<JobParticipationStatsDto>>> getJobStats() {
    var result = statisticsQueryService.getJobParticipationStats();
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
