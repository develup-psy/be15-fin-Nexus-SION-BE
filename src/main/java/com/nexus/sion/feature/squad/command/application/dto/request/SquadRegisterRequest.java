package com.nexus.sion.feature.squad.command.application.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadRegisterRequest {

  @NotBlank(message = "프로젝트 코드는 필수입니다.")
  private String projectCode;

  @NotBlank(message = "스쿼드 제목은 필수입니다.")
  private String title;

  private String description;

  @NotNull(message = "예상 기간은 필수입니다.")
  private BigDecimal estimatedDuration;

  @NotNull(message = "예상 비용은 필수입니다.")
  private BigDecimal estimatedCost;

  @NotEmpty(message = "개발자 목록은 1명 이상이어야 합니다.")
  private List<Developer> developers;
}