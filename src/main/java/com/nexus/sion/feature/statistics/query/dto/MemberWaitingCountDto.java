package com.nexus.sion.feature.statistics.query.dto;

import com.example.jooq.generated.enums.MemberGradeCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberWaitingCountDto {
  private MemberGradeCode gradeCode;
  private int waitingCount;
  private int totalCount;
}
