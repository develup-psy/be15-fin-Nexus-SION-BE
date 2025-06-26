package com.nexus.sion.feature.squad.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SquadUpdateRequest {

  @NotBlank(message = "수정할 스쿼드 코드가 필요합니다.")
  private String squadCode; // 수정할 스쿼드 코드

  @NotBlank(message = "프로젝트 코드는 필수입니다.")
  private String projectCode; // 프로젝트 코드

  @NotBlank(message = "스쿼드 제목은 필수입니다.")
  private String title; // 스쿼드 이름

  private String description; // 스쿼드 설명

  @NotEmpty(message = "구성원 목록은 1명 이상이어야 합니다.")
  private List<Member> members;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Member {
    @NotBlank(message = "사번은 필수입니다.")
    private String employeeIdentificationNumber; // 개발자 사번

    @NotNull(message = "직무-프로젝트 ID는 필수입니다.")
    private Long projectAndJobId; // 직무 ID
  }
}
