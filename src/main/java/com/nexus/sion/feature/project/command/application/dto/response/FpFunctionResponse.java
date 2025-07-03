package com.nexus.sion.feature.project.command.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FpFunctionResponse {
  @JsonProperty("fp_type")
  private String fpType;

  @JsonProperty("function_name")
  private String functionName;

  private String description;
  private String complexity;

  @JsonProperty("estimated_det")
  private int estimatedDet;

  @JsonProperty("estimated_ftr")
  private int estimatedFtr;

  private int score;
}
