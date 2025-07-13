package com.nexus.sion.feature.member.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgramResponse {
  private Long trainingId;
  private String trainingName;
  private String trainingDescription;
  private String trainingCategory;
  private String imageUrl;
  private String videoUrl;
}
