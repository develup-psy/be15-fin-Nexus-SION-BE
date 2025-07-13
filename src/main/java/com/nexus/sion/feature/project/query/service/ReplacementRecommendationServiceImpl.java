package com.nexus.sion.feature.project.query.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.query.repository.ReplacementRecommendationRepository;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import com.nexus.sion.feature.squad.query.util.CalculateSquad;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReplacementRecommendationServiceImpl implements ReplacementRecommendationService {

  private final ReplacementRecommendationRepository recommendationRepository;
  private final CalculateSquad calculateSquad;

  @Override
  public List<DeveloperSummary> recommendCandidates(String projectCode, String employeeId) {
    List<DeveloperSummary> rawCandidates =
        recommendationRepository.findCandidatesForReplacement(projectCode, employeeId);

    calculateSquad.applyWeightToCandidates(rawCandidates);

    return rawCandidates.stream()
        .sorted(Comparator.comparingDouble(DeveloperSummary::getWeight).reversed())
        .limit(5)
        .toList();
  }
}
