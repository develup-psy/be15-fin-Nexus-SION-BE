package com.nexus.sion.feature.member.query.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberListRequest {
  private int page;
  private int size;

  private String gradeCode;
  private String role;
  private String status; // ex: "AVAILABLE", "IN_PROJECT", "UNAVAILABLE"
  private String nameInitial;
  private String sortBy;
  private String sortDir;

  private String keyword;
}
