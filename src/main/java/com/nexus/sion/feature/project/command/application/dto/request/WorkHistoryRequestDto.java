package com.nexus.sion.feature.project.command.application.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkHistoryRequestDto {
  private String employeeIdentificationNumber;
  private String projectCode;
  private List<WorkHistoryItemDto> histories;
}
