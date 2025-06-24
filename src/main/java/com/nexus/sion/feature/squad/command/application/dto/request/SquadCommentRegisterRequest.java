package com.nexus.sion.feature.squad.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SquadCommentRegisterRequest {

    @NotBlank
    private String employeeIdentificationNumber;

    @NotBlank
    @Size(max = 500)
    private String content;
}
