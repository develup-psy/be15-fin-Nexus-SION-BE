package com.nexus.sion.feature.squad.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperSummary {

  private String id;
  private String name;
  private String grade;

  private double avgTechScore;
  private int domainCount;
  private Double weight;

  int monthlyUnitPrice;
  BigDecimal productivity;

  public void setWeight(Double weight) {
    this.weight = weight;
  }
}
