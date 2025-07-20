package com.nexus.sion.feature.project.query.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyProjectListRequest {

  private String employeeId;
  private int page = 0;
  private int size = 4;
  private List<String> statuses;
  private String sortBy;
  private String keyword;

  public MyProjectListRequest(
      String employeeId, int page, int size, List<String> statuses, String sortBy, String keyword) {
    this.employeeId = employeeId;
    this.page = page;
    this.size = size;
    this.statuses = statuses;
    this.sortBy = sortBy;
    this.keyword = keyword;
  }
}
