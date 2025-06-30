package com.nexus.sion.feature.squad.query.util;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;

@Service
public class CalculateSquad {
  private static final double TECH_STACK_WEIGHT_RATIO = 0.5;
  private static final double DOMAIN_MATCH_WEIGHT_RATIO = 0.5;

  public void applyWeightToCandidates(Map<String, List<DeveloperSummary>> candidatesPerJob) {
    for (Map.Entry<String, List<DeveloperSummary>> entry : candidatesPerJob.entrySet()) {
      List<DeveloperSummary> list = entry.getValue();

      // 정규화를 위한 최대값 구하기
      double maxTechScore =
          list.stream().mapToDouble(DeveloperSummary::getAvgTechScore).max().orElse(1.0);
      int maxDomainCount = list.stream().mapToInt(DeveloperSummary::getDomainCount).max().orElse(1);

      for (DeveloperSummary dev : list) {
        double techScoreRatio = dev.getAvgTechScore() / maxTechScore;
        double domainRatio = (double) dev.getDomainCount() / maxDomainCount;

        double weight =
            TECH_STACK_WEIGHT_RATIO * techScoreRatio + DOMAIN_MATCH_WEIGHT_RATIO * domainRatio;

        dev.setWeight(weight);
      }
    }
  }
}
