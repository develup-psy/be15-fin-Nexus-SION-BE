package com.nexus.sion.feature.project.command.application.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FPInferResponse {
  private String project_id;
  private List<FpFunctionResponse> functions;
  private int total_fp_score;
}
