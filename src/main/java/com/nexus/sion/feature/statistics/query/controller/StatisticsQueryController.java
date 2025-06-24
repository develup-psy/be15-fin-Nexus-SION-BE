package com.nexus.sion.feature.statistics.query.controller;

import java.util.List;

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

  private final StatisticsQueryService service;

  @PostMapping("/stack/member-count")
  public ApiResponse<List<TechStackCountDto>> getStackCount(@RequestBody List<String> stacks) {
    return ApiResponse.success(service.getStackMemberCounts(stacks));
  }

  @GetMapping("/all-tech-stacks")
  public ApiResponse<List<String>> getAllTechStacks() {
    return ApiResponse.success(service.findAllStackNames());
  }

  @GetMapping("/developers")
  public ApiResponse<PageResponse<DeveloperDto>> getAllDevelopers(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.success(service.getAllDevelopers(page, size));
  }

  @GetMapping("/stack/average-career")
  public ApiResponse<PageResponse<TechStackCareerDto>> getStackAverageCareerPaged(
      @RequestParam List<String> selectedStacks,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "techStackName") String sort,
      @RequestParam(defaultValue = "asc") String direction) {
    return ApiResponse.success(
        service.getStackAverageCareersPaged(selectedStacks, page, size, sort, direction));
  }

  @GetMapping("/stack/popular")
  public ApiResponse<PageResponse<PopularTechStackDto>> getPopularTechStacks(
      @RequestParam String period,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(name = "top", required = false) Integer top) {

    if (top != null) {
      return ApiResponse.success(service.getPopularTechStacksWithTop(period, top));
    }

    return ApiResponse.success(service.getPopularTechStacks(period, page, size));
  }
}
