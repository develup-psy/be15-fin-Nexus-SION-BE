package com.nexus.sion.feature.member.query.dto.response;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.TrainingProgram;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingRecommendationResponse {

  private Long trainingId;
  private String trainingName;
  private String trainingDescription;
  private String imageUrl;
  private String recommendationReason;
  private String category;
  private String videoUrl;

  public static TrainingRecommendationResponse from(TrainingProgram p, String reason) {
    return TrainingRecommendationResponse.builder()
        .trainingId(p.getTrainingId())
        .trainingName(p.getTrainingName())
        .trainingDescription(p.getTrainingDescription())
        .imageUrl(p.getImageUrl())
        .recommendationReason(reason)
        .category(p.getTrainingCategory())
        .videoUrl(p.getVideoUrl())
        .build();
  }

  public static TrainingRecommendationResponse from(TrainingProgramResponse p, String reason) {
    return TrainingRecommendationResponse.builder()
        .trainingId(p.getTrainingId())
        .trainingName(p.getTrainingName())
        .trainingDescription(p.getTrainingDescription())
        .imageUrl(p.getImageUrl())
        .recommendationReason(reason)
        .category(p.getTrainingCategory())
        .videoUrl(p.getVideoUrl())
        .build();
  }
}
