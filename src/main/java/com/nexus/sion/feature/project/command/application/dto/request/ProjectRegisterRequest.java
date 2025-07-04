package com.nexus.sion.feature.project.command.application.dto.request;

import java.time.LocalDate;
import java.util.List;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRegisterRequest {

  private String projectCode;
  private String domainName;
  private String description;
  private String title;
  private Long budget;
  private LocalDate startDate;
  private LocalDate expectedEndDate;
  private String clientCode;
  private Integer numberOfMembers;
  private String requestSpecificationUrl; // S3에서 받은 URL

  private List<JobInfo> jobs;

  public static ProjectRegisterRequest copyWithProjectCode(
      ProjectRegisterRequest original, String newProjectCode) {
    return ProjectRegisterRequest.builder()
        .projectCode(newProjectCode)
        .domainName(original.getDomainName())
        .description(original.getDescription())
        .title(original.getTitle())
        .budget(original.getBudget())
        .startDate(original.getStartDate())
        .expectedEndDate(original.getExpectedEndDate())
        .clientCode(original.getClientCode())
        .numberOfMembers(original.getNumberOfMembers())
        .requestSpecificationUrl(original.getRequestSpecificationUrl())
        .jobs(original.getJobs())
        .build();
  }

  @Getter
  @Setter
  public static class JobInfo {
    private String jobName;
    private int requiredNumber;
    private List<TechStackInfo> techStacks;
  }

  @Getter
  @Setter
  public static class TechStackInfo {
    private String techStackName;
    private Integer priority;
  }
}
