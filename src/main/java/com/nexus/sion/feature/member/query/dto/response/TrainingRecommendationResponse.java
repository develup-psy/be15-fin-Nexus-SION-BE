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
    return buildResponse(p.getTrainingId(), p.getTrainingName(), p.getTrainingDescription(),
            p.getImageUrl(), reason, p.getTrainingCategory(), p.getVideoUrl());
  }

  public static TrainingRecommendationResponse from(TrainingProgramResponse p, String reason) {
    return buildResponse(p.getTrainingId(), p.getTrainingName(), p.getTrainingDescription(),
            p.getImageUrl(), reason, p.getTrainingCategory(), p.getVideoUrl());
  }

  private static TrainingRecommendationResponse buildResponse(
          Long trainingId,
          String trainingName,
          String trainingDescription,
          String imageUrl,
          String reason,
          String category,
          String videoUrl
  ) {
    return TrainingRecommendationResponse.builder()
            .trainingId(trainingId)
            .trainingName(trainingName)
            .trainingDescription(trainingDescription)
            .imageUrl(imageUrl)
            .recommendationReason(reason)
            .category(category)
            .videoUrl(videoUrl)
            .build();
  }
}
