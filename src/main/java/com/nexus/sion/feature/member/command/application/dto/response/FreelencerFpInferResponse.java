package com.nexus.sion.feature.member.command.application.dto.response;

import com.nexus.sion.feature.project.command.application.dto.FunctionScore;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class FreelencerFpInferResponse {
    private List<FunctionScore> functions;
}
