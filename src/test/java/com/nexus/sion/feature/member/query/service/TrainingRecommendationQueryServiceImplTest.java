package com.nexus.sion.feature.member.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexus.sion.feature.member.query.dto.response.*;
import com.nexus.sion.feature.member.query.repository.MemberTechStackQueryRepository;
import com.nexus.sion.feature.member.query.repository.TrainingProgramQueryRepository;

class TrainingRecommendationQueryServiceImplTest {

  @Mock private MemberTechStackQueryRepository techStackRepo;

  @Mock private UserCertificateHistoryQueryService certQueryService;

  @Mock private TrainingProgramQueryRepository trainingRepo;

  @InjectMocks private TrainingRecommendationQueryServiceImpl service;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("교육 추천 성공 - 기술 스택과 자격증 기반")
  void recommendTrainingsFor_success() {
    // given
    String employeeId = "EMP001";

    // 기술 스택 점수
    given(techStackRepo.findTechStacksByEmployeeId(employeeId))
        .willReturn(
            List.of(
                new MemberTechStackResponse("Spring", 50),
                new MemberTechStackResponse("Redis", 10)));

    given(techStackRepo.findAllScoresForTech("Spring")).willReturn(List.of(30, 40, 50, 60, 70));
    given(techStackRepo.findAllScoresForTech("Redis")).willReturn(List.of(5, 15, 20, 25, 30));

    // 교육 프로그램
    given(trainingRepo.findByCategory("Spring-INTERMEDIATE"))
        .willReturn(
            List.of(
                new TrainingProgramResponse(
                    1L, "Spring 중급", "중급 과정", "Spring-INTERMEDIATE", "url1", "video1")));
    given(trainingRepo.findByCategory("Redis-BEGINNER"))
        .willReturn(
            List.of(
                new TrainingProgramResponse(
                    2L, "Redis 초급", "초급 과정", "Redis-BEGINNER", "url2", "video2")));

    // 자격증
    given(certQueryService.findAllCertificateNames()).willReturn(List.of("정보처리기사", "SQLD"));
    given(certQueryService.findOwnedCertificateNamesByEmployee(employeeId))
        .willReturn(List.of("SQLD"));
    given(trainingRepo.findByCategoryIn(List.of("정보처리기사")))
        .willReturn(
            List.of(
                new TrainingProgramResponse(
                    3L, "정보처리기사 대비", "필기/실기 준비", "정보처리기사", "url3", "video3")));

    // when
    List<TrainingRecommendationResponse> result = service.recommendTrainingsFor(employeeId);

    // then
    assertThat(result).hasSize(3);
    assertThat(result).extracting("trainingName").contains("Spring 중급", "Redis 초급", "정보처리기사 대비");
  }
}
