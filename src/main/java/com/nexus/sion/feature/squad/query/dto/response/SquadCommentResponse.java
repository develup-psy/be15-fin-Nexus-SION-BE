package com.nexus.sion.feature.squad.query.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadCommentResponse {

  private Long commentId;
  private String employeeName;
  private String employeeIdentificationNumber;
  private String content;
  private LocalDateTime createdAt;
}
