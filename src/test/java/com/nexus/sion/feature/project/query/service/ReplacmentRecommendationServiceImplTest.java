package com.nexus.sion.feature.project.query.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexus.sion.feature.project.query.repository.ReplacementRecommendationRepository;
import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import com.nexus.sion.feature.squad.query.util.CalculateSquad;

@ExtendWith(MockitoExtension.class)
public class ReplacmentRecommendationServiceImplTest {

  @Mock private ReplacementRecommendationRepository recommendationRepository;
  @Mock private CalculateSquad calculateSquad;

  @InjectMocks private ReplacementRecommendationServiceImpl service;

  @Test
  @DisplayName("프로젝트 인원 대체 추천 성공 - 상위 5명 반환")
  void recommendCandidates_success_returnsTop5() {
    // given
    String projectCode = "PJT-001";
    String employeeId = "EMP001";
    List<DeveloperSummary> candidates =
        List.of(
            createCandidate("DEV01", 0.9),
            createCandidate("DEV02", 0.85),
            createCandidate("DEV03", 0.95),
            createCandidate("DEV04", 0.8),
            createCandidate("DEV05", 0.7),
            createCandidate("DEV06", 0.6) // 6명 → 상위 5명 반환 검증
            );

    when(recommendationRepository.findCandidatesForReplacement(projectCode, employeeId))
        .thenReturn(candidates);

    // 가중치 계산은 내부 void 처리이므로 그냥 호출 여부만 확인
    doNothing().when(calculateSquad).applyWeightToCandidates(anyList());

    // when
    List<DeveloperSummary> result = service.recommendCandidates(projectCode, employeeId);

    // then
    assertThat(result).hasSize(5);
    assertThat(result.get(0).getId()).isEqualTo("DEV03"); // weight 가장 높은 순
    assertThat(result.stream().map(DeveloperSummary::getId)).doesNotContain("DEV06");

    verify(recommendationRepository).findCandidatesForReplacement(projectCode, employeeId);
    verify(calculateSquad).applyWeightToCandidates(anyList());
  }

  @Test
  @DisplayName("프로젝트 인원 대체 추천 - 후보군이 5명 이하인 경우 전체 반환")
  void recommendCandidates_whenLessThan5Candidates_returnsAll() {
    String projectCode = "PJT-001";
    String employeeId = "EMP001";
    List<DeveloperSummary> candidates =
        List.of(
            createCandidate("DEV01", 0.9),
            createCandidate("DEV02", 0.85),
            createCandidate("DEV03", 0.95));

    when(recommendationRepository.findCandidatesForReplacement(projectCode, employeeId))
        .thenReturn(candidates);
    doNothing().when(calculateSquad).applyWeightToCandidates(anyList());

    List<DeveloperSummary> result = service.recommendCandidates(projectCode, employeeId);

    assertThat(result).hasSize(3);
    assertThat(result.get(0).getWeight()).isGreaterThanOrEqualTo(result.get(1).getWeight());
    verify(recommendationRepository).findCandidatesForReplacement(projectCode, employeeId);
    verify(calculateSquad).applyWeightToCandidates(anyList());
  }

  @Test
  @DisplayName("프로젝트 인원 대체 추천 - 후보군 없음 시 빈 리스트 반환")
  void recommendCandidates_whenNoCandidates_returnsEmptyList() {
    String projectCode = "PJT-002";
    String employeeId = "EMP002";

    when(recommendationRepository.findCandidatesForReplacement(projectCode, employeeId))
        .thenReturn(Collections.emptyList());
    doNothing().when(calculateSquad).applyWeightToCandidates(anyList());

    List<DeveloperSummary> result = service.recommendCandidates(projectCode, employeeId);

    assertThat(result).isEmpty();
    verify(recommendationRepository).findCandidatesForReplacement(projectCode, employeeId);
    verify(calculateSquad).applyWeightToCandidates(anyList());
  }

  private DeveloperSummary createCandidate(String id, double weight) {
    return DeveloperSummary.builder().id(id).weight(weight).build();
  }
}
