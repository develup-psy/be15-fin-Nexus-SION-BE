package com.nexus.sion.feature.project.query.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.example.jooq.generated.enums.ProjectAnalysisStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectDetailResponse {
  private String title;
  private String domainName;
  private String requestSpecificationUrl;
  private String description;
  private LocalDate startDate; // 변경
  private LocalDate endDate;
  private String budget; // ex: ₩29,000,000
  private List<String> techStacks;
  private List<SquadMemberInfo> members;
  private String status;
  private ProjectAnalysisStatus analysisStatus;
  private String squadCode;

  @Getter
  @AllArgsConstructor
  public static class SquadMemberInfo {
    private String employeeId;
    private Integer isLeader;
    private String imageUrl;
    private String name;
    private String job;
  }
}
