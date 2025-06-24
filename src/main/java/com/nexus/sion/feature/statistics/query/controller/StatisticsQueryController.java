package com.nexus.sion.feature.statistics.query.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

  private final StatisticsQueryService service;

  @PostMapping("/stack/member-count")
  public ResponseEntity<ApiResponse<List<TechStackCountDto>>> getStackCount(
      @RequestBody List<String> stacks) {
    var response = service.getStackMemberCounts(stacks);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @GetMapping("/all-tech-stacks")
  public ResponseEntity<ApiResponse<List<String>>> getAllTechStacks() {
    var response = service.findAllStackNames();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/developers")
  public ResponseEntity<ApiResponse<PageResponse<DeveloperDto>>> getAllDevelopers(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
    var response = service.getAllDevelopers(page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/stack/average-career")
  public ResponseEntity<ApiResponse<PageResponse<TechStackCareerDto>>> getStackAverageCareerPaged(
      @RequestParam List<String> selectedStacks,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "techStackName") String sort,
      @RequestParam(defaultValue = "asc") String direction) {
    var response = service.getStackAverageCareersPaged(selectedStacks, page, size, sort, direction);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/stack/popular")
  public ResponseEntity<ApiResponse<PageResponse<PopularTechStackDto>>> getPopularTechStacks(
      @RequestParam String period,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(name = "top", required = false) Integer top) {

    if (top != null) {
      var response = service.getPopularTechStacksWithTop(period, top);
      return ResponseEntity.ok(ApiResponse.success(response));
    }

    var response = service.getPopularTechStacks(period, page, size);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
