package com.nexus.sion.feature.member.command.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InitialScoreSetRequset {
    List<InitialScoreDto> initialScores;
}
