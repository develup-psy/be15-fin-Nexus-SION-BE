package com.nexus.sion.feature.project.command.application.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FpFunctionResponse {
  private String fp_type;
  private String function_name;
  private String description;
  private String complexity;
  private int estimated_det;
  private int estimated_ftr;
  private int score;
}
