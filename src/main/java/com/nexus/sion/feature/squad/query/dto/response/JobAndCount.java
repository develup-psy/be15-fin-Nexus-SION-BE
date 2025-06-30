package com.nexus.sion.feature.squad.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobAndCount {
  private String jobName;
  private Integer requiredNumber;
}
