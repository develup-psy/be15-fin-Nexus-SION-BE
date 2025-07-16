package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.Tables.TRAINING_RECOMMENDATION;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.member.query.dto.response.TrainingRecommendationResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TrainingRecommendationQueryRepository {

  private final DSLContext dsl;

  // 교육 카테고리 목록
  public List<String> findCategories() {
    return dsl.selectDistinct(TRAINING_RECOMMENDATION.TRAINING_CATEGORY)
        .from(TRAINING_RECOMMENDATION)
        .fetchInto(String.class);
  }

  // 특정 카테고리의 교육 목록
  public List<TrainingRecommendationResponse> findByCategory(String category) {
    return baseSelect()
        .where(TRAINING_RECOMMENDATION.TRAINING_CATEGORY.eq(category))
        .fetchInto(TrainingRecommendationResponse.class);
  }

  // 여러 카테고리 조건 (자격증 기반 추천용)
  public List<TrainingRecommendationResponse> findByCategoryIn(List<String> categories) {
    return baseSelect()
        .where(TRAINING_RECOMMENDATION.TRAINING_CATEGORY.in(categories))
        .fetchInto(TrainingRecommendationResponse.class);
  }

  // 전체 교육 목록
  public List<TrainingRecommendationResponse> findAll() {
    return baseSelect().fetchInto(TrainingRecommendationResponse.class);
  }

  // 공통 SELECT 로직
  private org.jooq.SelectJoinStep<?> baseSelect() {
    return dsl.select(
            TRAINING_RECOMMENDATION.TRAINING_RECOMMENDATION_ID,
            TRAINING_RECOMMENDATION.TRAINING_NAME,
            TRAINING_RECOMMENDATION.TRAINING_DESCRIPTION,
            TRAINING_RECOMMENDATION.TRAINING_CATEGORY,
            TRAINING_RECOMMENDATION.IMAGE_URL,
            TRAINING_RECOMMENDATION.VIDEO_URL)
        .from(TRAINING_RECOMMENDATION);
  }
}
