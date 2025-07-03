package com.nexus.sion.feature.project.command.application.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FPInferResponse {
  @JsonProperty("project_id")
  private String projectId;

  private List<FpFunctionResponse> functions;

  @JsonProperty("total_fp_score")
  private int totalFpScore;
}
