package com.nexus.sion.feature.project.command.application.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionScoreDTO {
    private String employeeIdentificationNumber;
    private String projectCode;
    private List<FunctionScore> functionScores;
}
