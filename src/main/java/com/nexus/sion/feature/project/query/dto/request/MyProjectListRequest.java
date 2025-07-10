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

  public MyProjectListRequest(String employeeId, int page, int size, List<String> statuses) {
    this.employeeId = employeeId;
    this.page = page;
    this.size = size;
    this.statuses = statuses;
  }
}
