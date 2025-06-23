package com.nexus.sion.feature.statistics.query.controller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;
import com.nexus.sion.feature.statistics.query.service.StatisticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(service.getAllDevelopers(page, size));
    }
}
