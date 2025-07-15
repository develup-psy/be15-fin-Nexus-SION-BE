package com.nexus.sion.feature.member.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexus.sion.feature.member.query.dto.response.*;
import com.nexus.sion.feature.member.query.repository.MemberTechStackQueryRepository;
import com.nexus.sion.feature.member.query.repository.TrainingRecommendationQueryRepository;

class TrainingRecommendationQueryServiceImplTest {

  @Mock private MemberTechStackQueryRepository techStackRepo;

  @Mock private UserCertificateHistoryQueryService certQueryService;

  @Mock private TrainingRecommendationQueryRepository trainingRepo;

  @InjectMocks private TrainingRecommendationQueryServiceImpl service;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("기술 스택과 자격증 기반 교육 추천 성공")
  void recommendTrainingsFor_success() {
    // given
    String employeeId = "EMP001";

    // 기술 스택
    given(techStackRepo.findTechStacksByEmployeeId(employeeId))
        .willReturn(List.of(new MemberTechStackResponse("Spring", 50)));

    given(techStackRepo.findAllScoresForTech("Spring"))
        .willReturn(List.of(10, 20, 30, 40, 50, 60, 70));

    given(trainingRepo.findByCategory("Spring-INTERMEDIATE"))
        .willReturn(
            List.of(
                new TrainingRecommendationResponse(
                    1L, "Spring 중급", "중급 과정", "Spring-INTERMEDIATE", "img", "video", null)));

    // 자격증
    given(certQueryService.findAllCertificateNames()).willReturn(List.of("SQLD", "정보처리기사"));
    given(certQueryService.findOwnedCertificateNamesByEmployee(employeeId))
        .willReturn(List.of("SQLD")); // 정보처리기사는 미보유

    given(trainingRepo.findByCategoryIn(List.of("정보처리기사")))
        .willReturn(
            List.of(
                new TrainingRecommendationResponse(
                    2L, "정보처리기사 대비", "준비 과정", "정보처리기사", "img2", "video2", null)));

    // when
    List<TrainingRecommendationResponse> result = service.recommendTrainingsFor(employeeId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting("trainingName")
        .containsExactlyInAnyOrder("Spring 중급", "정보처리기사 대비");
  }

  @Test
  @DisplayName("기술 스택 정보가 없는 경우")
  void recommendTrainingsFor_noTechStack_returnsEmptyList() {
    // given
    String employeeId = "EMP002";

    given(techStackRepo.findTechStacksByEmployeeId(employeeId)).willReturn(Collections.emptyList());
    given(certQueryService.findOwnedCertificateNamesByEmployee(employeeId))
        .willReturn(Collections.emptyList());

    // when
    List<TrainingRecommendationResponse> result = service.recommendTrainingsFor(employeeId);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("기술 스택 점수 분포가 없는 경우")
  void recommendTrainingsFor_missingTechScores_throwsException() {
    // given
    String employeeId = "EMP003";

    given(techStackRepo.findTechStacksByEmployeeId(employeeId))
        .willReturn(List.of(new MemberTechStackResponse("Java", 70)));

    given(techStackRepo.findAllScoresForTech("Java")).willReturn(Collections.emptyList());

    // when & then
    assertThatThrownBy(() -> service.recommendTrainingsFor(employeeId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Java 점수 분포가 없습니다");
  }

  @Test
  @DisplayName("보유하지 않은 자격증이 없는 경우")
  void recommendTrainingsFor_noUnownedCertificates_returnsTechBasedOnly() {
    // given
    String employeeId = "EMP004";

    given(techStackRepo.findTechStacksByEmployeeId(employeeId))
        .willReturn(List.of(new MemberTechStackResponse("Spring", 50)));

    given(techStackRepo.findAllScoresForTech("Spring")).willReturn(List.of(30, 40, 50));

    given(trainingRepo.findByCategory("Spring-INTERMEDIATE"))
        .willReturn(
            List.of(
                new TrainingRecommendationResponse(
                    10L,
                    "Spring 중급",
                    "Spring 중급 과정",
                    "Spring-INTERMEDIATE",
                    "image",
                    "video",
                    "기술 기반 추천")));

    given(certQueryService.findAllCertificateNames()).willReturn(List.of("SQLD"));
    given(certQueryService.findOwnedCertificateNamesByEmployee(employeeId))
        .willReturn(List.of("SQLD"));

    // when
    List<TrainingRecommendationResponse> result = service.recommendTrainingsFor(employeeId);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTrainingName()).isEqualTo("Spring 중급");
  }
}
