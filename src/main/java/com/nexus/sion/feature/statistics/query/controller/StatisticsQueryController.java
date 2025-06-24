package com.nexus.sion.feature.statistics.query.controller;

import java.util.List;

import com.nexus.sion.feature.techstack.query.service.TechStackQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.PopularTechStackDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCareerDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;
import com.nexus.sion.feature.statistics.query.service.StatisticsQueryService;

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
    public ApiResponse<List<String>> getAllTechStacks() {
        return ApiResponse.success(techStackQueryService.findAllStackNames());
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
    var response = statisticsQueryService.getStackAverageCareersPaged(selectedStacks, page, size, sort, direction);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/stack/popular")
  public ResponseEntity<ApiResponse<PageResponse<PopularTechStackDto>>> getPopularTechStacks(
      @RequestParam String period,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(name = "top", required = false) Integer top) {

    if (top != null) {
      var response = statisticsQueryService.getPopularTechStacksWithTop(period, top);
      return ResponseEntity.ok(ApiResponse.success(response));
    }

    var response = statisticsQueryService.getPopularTechStacks(period, page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
