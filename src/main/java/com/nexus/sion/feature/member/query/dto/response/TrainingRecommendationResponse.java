package com.nexus.sion.feature.member.query.dto.response;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.TrainingRecommendation;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingRecommendationResponse {

  private Long trainingId;
  private String trainingName;
  private String trainingDescription;
  private String trainingCategory;
  private String imageUrl;
  private String videoUrl;
  private String recommendationReason;

  public static TrainingRecommendationResponse from(TrainingRecommendation program) {
    return TrainingRecommendationResponse.builder()
        .trainingId(program.getTrainingId())
        .trainingName(program.getTrainingName())
        .trainingDescription(program.getTrainingDescription())
        .trainingCategory(program.getTrainingCategory())
        .imageUrl(program.getImageUrl())
        .videoUrl(program.getVideoUrl())
        .build();
  }

  public static TrainingRecommendationResponse from(TrainingRecommendation program, String reason) {
    return TrainingRecommendationResponse.builder()
        .trainingId(program.getTrainingId())
        .trainingName(program.getTrainingName())
        .trainingDescription(program.getTrainingDescription())
        .trainingCategory(program.getTrainingCategory())
        .imageUrl(program.getImageUrl())
        .videoUrl(program.getVideoUrl())
        .recommendationReason(reason)
        .build();
  }

  public TrainingRecommendation toEntity() {
    return TrainingRecommendation.builder()
        .trainingId(trainingId)
        .trainingName(trainingName)
        .trainingDescription(trainingDescription)
        .trainingCategory(trainingCategory)
        .imageUrl(imageUrl)
        .videoUrl(videoUrl)
        .build();
  }
}
