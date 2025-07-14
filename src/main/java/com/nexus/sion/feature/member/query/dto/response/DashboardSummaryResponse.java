package com.nexus.sion.feature.member.query.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record DashboardSummaryResponse(
    List<PendingProject> pendingProjects,
    List<AnalyzingProject> analyzingProjects,
    List<TopDeveloper> topDevelopers,
    List<FreelancerSummary> freelancerTop5,
    DeveloperAvailability developerAvailability,
    List<TechStackDemand> techStackDemand) {
  @Builder
  public record PendingProject(
      String projectCode,
      String title,
      String description,
      Map<String, Integer> roles, // ex) {"백엔드": 2, "프론트엔드": 1}
      Long budget,
      String domainName,
      LocalDate startDate) {}

  public record AnalyzingProject(String id, String name, LocalDate analysisStartTime) {}

  @Builder
  public record TopDeveloper(
      String id,
      String name,
      String grade,
      BigDecimal productivity,
      List<String> techStacks,
      String profileUrl) {}

  public record FreelancerSummary(
      String id, String name, int career_years, String grade, String profileUrl) {}

  public record DeveloperAvailability(
      int totalAvailable, List<GradeDistribution> gradeDistribution, List<String> availableStacks) {
    public record GradeDistribution(String grade, int count) {}
  }

  public record TechStackDemand(String name, int count) {}
}
