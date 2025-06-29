package com.nexus.sion.feature.member.command.application.dto.request;

import java.util.List;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class UnitPriceSetRequest {
  List<GradeDto> grades;
}
