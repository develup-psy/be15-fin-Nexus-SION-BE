package com.nexus.sion.feature.statistics.query.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperDto {
  private String profileImageUrl;
  private String name;
  private String position;
  private String department;
  private String code;
  private String grade;
  private String status;

  // 추가
  private List<String> techStacks;
}
