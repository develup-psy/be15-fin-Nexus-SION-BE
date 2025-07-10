package com.nexus.sion.feature.member.query.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@Setter
public class MemberSquadSearchRequest {
  private String keyword;
  private String status;
  private List<String> grades;
  private List<String> stacks;
  private List<String> memberRoles; // INSIDER, OUTSIDER, ADMIN

  private String sortBy = "grade"; // 기본 정렬
  private String sortDir = "asc"; // 오름차순
  private Integer page;
  private Integer size;
}
