package com.nexus.sion.feature.techstack.query.cotroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.techstack.query.dto.response.TechStackListResponse;
import com.nexus.sion.feature.techstack.query.service.TechStackQueryService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tech-stack")
public class TechStackQueryController {

  private final TechStackQueryService techStackQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<TechStackListResponse>> getAllTechStacks() {
    return ResponseEntity.ok(
        ApiResponse.success(new TechStackListResponse(techStackQueryService.findAllStackNames())));
  }

  @GetMapping("/autocomplete")
  public ResponseEntity<ApiResponse<TechStackListResponse>> autocomplete(
          @RequestParam String keyword
  ) {
    List<String> results = techStackQueryService.autocomplete(keyword);
    return ResponseEntity.ok(ApiResponse.success(
            new TechStackListResponse(results))
    );
  }
}
