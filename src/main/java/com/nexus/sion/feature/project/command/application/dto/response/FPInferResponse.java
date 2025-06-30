package com.nexus.sion.feature.project.command.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FPInferResponse {
    private String  project_id;
    private List<FpFunctionResponse> functions;
    private int total_fp_score;
}
