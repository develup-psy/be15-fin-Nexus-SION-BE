package com.nexus.sion.feature.project.command.application.dto;

import java.util.List;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionScore {
  private String functionName;
  private String description;
  private String fpType;
  private int det;
  private int ftrOrRet;
  private List<String> stacks;
}
