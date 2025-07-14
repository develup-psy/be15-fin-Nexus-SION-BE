package com.nexus.sion.feature.member.query.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.dto.response.TrainingRecommendationResponse;
import com.nexus.sion.feature.member.query.service.TrainingRecommendationQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
public class TrainingRecommendationQueryController {

  private final TrainingRecommendationQueryService trainingRecommendationQueryService;

  @GetMapping("/recommend/me")
  public ApiResponse<List<TrainingRecommendationResponse>> recommendTrainings(
      @AuthenticationPrincipal UserDetails userDetails) {
    String employeeId = userDetails.getUsername();
    List<TrainingRecommendationResponse> result =
        trainingRecommendationQueryService.recommendTrainingsFor(employeeId);
    return ApiResponse.success(result);
  }
}
