package com.nexus.sion.feature.project.query.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplacementRecommendationRequest {

    @NotBlank(message = "프로젝트 코드는 필수입니다.")
    private String projectCode;

    @NotBlank(message = "대체 대상 사원 ID는 필수입니다.")
    private String leavingMember;
}