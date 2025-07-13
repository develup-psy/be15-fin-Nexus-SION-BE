package com.nexus.sion.feature.project.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SquadReplacementRequest {
    @NotBlank(message = "스쿼드 코드는 필수입니다.")
    private String squadCode;
    @NotBlank(message = "대체 대상 사원 번호는 필수입니다.")
    private String oldEmployeeId;
    @NotBlank(message = "보충 대상 사원 번호는 필수입니다.")
    private String newEmployeeId;
}
