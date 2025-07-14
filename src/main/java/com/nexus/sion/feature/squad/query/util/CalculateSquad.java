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
    for (List<DeveloperSummary> developers : candidatesPerJob.values()) {
      applyWeight(developers);
    }
  }

  public void applyWeightToCandidates(List<DeveloperSummary> developers) {
    applyWeight(developers);
  }

  private void applyWeight(List<DeveloperSummary> list) {
    if (list == null || list.isEmpty()) return;

    double minTechScore =
        list.stream().mapToDouble(DeveloperSummary::getAvgTechScore).min().orElse(0);
    double maxTechScore =
        list.stream().mapToDouble(DeveloperSummary::getAvgTechScore).max().orElse(1);
    double techRange = (maxTechScore - minTechScore == 0) ? 1 : (maxTechScore - minTechScore);

    double minDomain = list.stream().mapToInt(DeveloperSummary::getDomainCount).min().orElse(0);
    double maxDomain = list.stream().mapToInt(DeveloperSummary::getDomainCount).max().orElse(1);
    double domainRange = (maxDomain - minDomain == 0) ? 1 : (maxDomain - minDomain);

    for (DeveloperSummary dev : list) {
      double techNormalized = (dev.getAvgTechScore() - minTechScore) / techRange;
      double domainNormalized = (dev.getDomainCount() - minDomain) / domainRange;

      double weight =
          TECH_STACK_WEIGHT_RATIO * techNormalized + DOMAIN_MATCH_WEIGHT_RATIO * domainNormalized;

      dev.setWeight(weight);
    }
  }
}
