package com.nexus.sion.feature.project.command.application.dto.request;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {

  private String projectCode;
  private String domainName;
  private String description;
  private String title;
  private Long budget;
  private LocalDate startDate;
  private LocalDate expectedEndDate;
  private Integer numberOfMembers;
  private String requestSpecificationUrl;

  // ✅ jobTechStacks 제거: 기술스택 수정도 불가
}
