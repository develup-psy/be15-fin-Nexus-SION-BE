package com.nexus.sion.feature.techstack.query.cotroller;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;
import com.nexus.sion.feature.statistics.query.service.StatisticsQueryService;
import com.nexus.sion.feature.techstack.query.dto.request.TechStackListRequset;
import com.nexus.sion.feature.techstack.query.dto.response.TechStackListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tech-stack")
public class TechStackQueryController {

    private final StatisticsQueryService statisticsQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<TechStackListResponse>> getAllTechStacks() {
        return ResponseEntity
                .ok(ApiResponse
                        .success(new TechStackListResponse(statisticsQueryService
                                .findAllStackNames())));
    }
}
