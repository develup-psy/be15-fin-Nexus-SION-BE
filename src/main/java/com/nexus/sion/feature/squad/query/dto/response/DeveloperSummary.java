package com.nexus.sion.feature.squad.query.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperSummary {

  private String id;
  private String name;
  private String grade;
  private String imageUrl;

  private double avgTechScore;
  private int domainCount;
  private Double weight;

  int monthlyUnitPrice;
  BigDecimal productivity;

  public void setWeight(Double weight) {
    this.weight = weight;
  }
}
