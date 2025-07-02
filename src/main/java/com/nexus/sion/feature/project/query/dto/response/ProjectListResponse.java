package com.nexus.sion.feature.project.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectListResponse {
  private String projectCode;
  private String title;
  private String description;
  private String startDate;
  private String endDate;
  private int period;
  private String status;
  private String domainName;
  private Integer hrCount;
}
