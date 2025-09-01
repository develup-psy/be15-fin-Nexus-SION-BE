package com.nexus.sion.feature.project.command.application.dto.request;

import java.math.BigDecimal;
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
  private BigDecimal budget;
  private LocalDate startDate;
  private LocalDate expectedEndDate;
  private Integer numberOfMembers;
  private String requestSpecificationUrl;

}
