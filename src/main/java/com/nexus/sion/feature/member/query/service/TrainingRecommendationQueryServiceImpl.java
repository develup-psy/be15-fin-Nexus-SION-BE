package com.nexus.sion.feature.member.query.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.query.dto.response.MemberTechStackResponse;
import com.nexus.sion.feature.member.query.dto.response.TrainingProgramResponse;
import com.nexus.sion.feature.member.query.dto.response.TrainingRecommendationResponse;
import com.nexus.sion.feature.member.query.repository.MemberTechStackQueryRepository;
import com.nexus.sion.feature.member.query.repository.TrainingProgramQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainingRecommendationQueryServiceImpl implements TrainingRecommendationQueryService {

  private final MemberTechStackQueryRepository memberTechStackQueryRepository;
  private final UserCertificateHistoryQueryService userCertificateHistoryQueryService;
  private final TrainingProgramQueryRepository trainingProgramQueryRepository;

  @Override
  public List<TrainingRecommendationResponse> recommendTrainingsFor(String employeeId) {
    List<TrainingRecommendationResponse> recommendations = new ArrayList<>();

    // 1. 기술 스택 점수 기반 추천
    List<MemberTechStackResponse> techStackList =
        memberTechStackQueryRepository.findTechStacksByEmployeeId(employeeId);
    Map<String, Integer> myScores =
        techStackList.stream()
            .collect(
                Collectors.toMap(
                    MemberTechStackResponse::techStackName, MemberTechStackResponse::score));

    for (Map.Entry<String, Integer> entry : myScores.entrySet()) {
      String tech = entry.getKey();
      int myScore = entry.getValue();
      List<Integer> allScores = memberTechStackQueryRepository.findAllScoresForTech(tech);

      if (allScores.isEmpty()) continue;

      int percentile = calculatePercentile(myScore, allScores);
      String level = getDifficultyLevel(percentile);

      List<TrainingProgramResponse> programs =
          trainingProgramQueryRepository.findByCategory(tech + "-" + level);

      recommendations.addAll(
          programs.stream()
              .map(
                  p ->
                      TrainingRecommendationResponse.from(
                          p, tech + " 점수 백분위 " + percentile + "%로 추천"))
              .collect(Collectors.toList()));
    }

    // 2. 자격증 미보유 기반 추천
    List<String> allCerts = userCertificateHistoryQueryService.findAllCertificateNames();
    List<String> ownedCerts =
        userCertificateHistoryQueryService.findOwnedCertificateNamesByEmployee(employeeId);
    List<String> missingCerts =
        allCerts.stream().filter(cert -> !ownedCerts.contains(cert)).collect(Collectors.toList());

    List<TrainingProgramResponse> certPrograms =
        trainingProgramQueryRepository.findByCategoryIn(missingCerts);
    recommendations.addAll(
        certPrograms.stream()
            .map(
                p ->
                    TrainingRecommendationResponse.from(
                        p, p.getTrainingCategory() + " 자격증 미보유로 추천"))
            .collect(Collectors.toList()));

    return recommendations;
  }

  private int calculatePercentile(int myScore, List<Integer> all) {
    long below = all.stream().filter(score -> score < myScore).count();
    return all.isEmpty() ? 0 : (int) ((below * 100.0) / all.size());
  }

  private String getDifficultyLevel(int percentile) {
    if (percentile < 30) return "BEGINNER";
    if (percentile <= 70) return "INTERMEDIATE";
    return "ADVANCED";
  }
}
