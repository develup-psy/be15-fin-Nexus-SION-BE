// ProjectInfoDto.java
package com.nexus.sion.feature.project.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProjectInfoDto {
  private final String projectCode;
  private final String projectTitle;
  private final LocalDate startDate;
  private final LocalDate endDate;
}
