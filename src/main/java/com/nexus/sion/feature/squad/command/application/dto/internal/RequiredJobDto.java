package com.nexus.sion.feature.squad.command.application.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface RequiredJobDto {
    Long getProjectAndJobId();
    Integer getRequiredNumber();
}
