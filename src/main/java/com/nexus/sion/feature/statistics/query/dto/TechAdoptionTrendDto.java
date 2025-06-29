package com.nexus.sion.feature.statistics.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechAdoptionTrendDto {
  private String techStackName; // 기술 스택 이름
  private Integer year; // 연도
  private Integer quarter; // 분기 (1~4)
  private Long projectCount; // 해당 분기 기술 스택 사용된 프로젝트 수
  private Double percentage; // 전체 프로젝트 대비 기술 스택의 사용 비율 (%)
  private double totalPercentage; // 총 도입률

  // 총 도입률 세터 추가
  public void setTotalPercentage(double totalPercentage) {
    this.totalPercentage = totalPercentage;
  }
}
