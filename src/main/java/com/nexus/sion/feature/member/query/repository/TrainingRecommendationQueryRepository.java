package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.Tables.TRAINING_PROGRAM;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.TrainingRecommendationResponse;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TrainingRecommendationQueryRepository {

  private final DSLContext dsl;

  // 교육 카테고리 목록
  public List<String> findCategories() {
    return dsl.selectDistinct(TRAINING_PROGRAM.TRAINING_CATEGORY)
        .from(TRAINING_PROGRAM)
        .fetchInto(String.class);
  }

  // 특정 카테고리의 교육 목록
  public List<TrainingRecommendationResponse> findByCategory(String category) {
    return baseSelect()
        .where(TRAINING_PROGRAM.TRAINING_CATEGORY.eq(category))
        .fetchInto(TrainingRecommendationResponse.class);
  }

  // 여러 카테고리 조건 (자격증 기반 추천용)
  public List<TrainingRecommendationResponse> findByCategoryIn(List<String> categories) {
    return baseSelect()
        .where(TRAINING_PROGRAM.TRAINING_CATEGORY.in(categories))
        .fetchInto(TrainingRecommendationResponse.class);
  }

  // 전체 교육 목록
  public List<TrainingRecommendationResponse> findAll() {
    return baseSelect().fetchInto(TrainingRecommendationResponse.class);
  }

  // 공통 SELECT 로직
  private org.jooq.SelectJoinStep<?> baseSelect() {
    return dsl.select(
            TRAINING_PROGRAM.TRAINING_ID,
            TRAINING_PROGRAM.TRAINING_NAME,
            TRAINING_PROGRAM.TRAINING_DESCRIPTION,
            TRAINING_PROGRAM.TRAINING_CATEGORY,
            TRAINING_PROGRAM.IMAGE_URL,
            TRAINING_PROGRAM.VIDEO_URL)
        .from(TRAINING_PROGRAM);
  }
}
