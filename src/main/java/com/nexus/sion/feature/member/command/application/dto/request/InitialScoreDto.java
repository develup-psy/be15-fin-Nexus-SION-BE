package com.nexus.sion.feature.member.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class InitialScoreDto {
    long id;
    @NotBlank
    int years;
    @NotBlank
    int score;
}
