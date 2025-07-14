package com.nexus.sion.feature.member.command.application.dto.response;

import java.util.List;

import com.nexus.sion.feature.project.command.application.dto.FunctionScore;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FreelencerFpInferResponse {
  private List<FunctionScore> functions;
}
