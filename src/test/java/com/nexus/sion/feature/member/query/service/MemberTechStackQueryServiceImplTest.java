package com.nexus.sion.feature.member.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexus.sion.feature.member.query.dto.response.MemberTechStackResponse;
import com.nexus.sion.feature.member.query.repository.MemberTechStackQueryRepository;

@ExtendWith(MockitoExtension.class)
class MemberTechStackQueryServiceImplTest {

  @Mock private MemberTechStackQueryRepository memberTechStackQueryRepository;

  @InjectMocks private MemberTechStackQueryServiceImpl memberTechStackQueryService;

  @DisplayName("사번으로 보유 기술스택을 조회한다")
  @Test
  void getTechStacks_success() {
    // given
    String employeeId = "DEV123";
    List<MemberTechStackResponse> expected =
        List.of(new MemberTechStackResponse("Java", 90), new MemberTechStackResponse("Spring", 85));

    when(memberTechStackQueryRepository.findTechStacksByEmployeeId(employeeId))
        .thenReturn(expected);

    // when
    List<MemberTechStackResponse> result = memberTechStackQueryService.getTechStacks(employeeId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).techStackName()).isEqualTo("Java");
    assertThat(result.get(0).score()).isEqualTo(90);

    verify(memberTechStackQueryRepository).findTechStacksByEmployeeId(employeeId);
  }
}
