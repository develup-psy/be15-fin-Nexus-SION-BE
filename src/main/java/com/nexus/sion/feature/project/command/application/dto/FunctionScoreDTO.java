package com.nexus.sion.feature.project.command.application.dto;

import java.util.List;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionScoreDTO {
  private String employeeIdentificationNumber;
  private String projectCode;
  private List<FunctionScore> functionScores;
}
