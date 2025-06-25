package com.nexus.sion.feature.member.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.nexus.sion.feature.member.query.dto.response.DepartmentResponse;
import com.nexus.sion.feature.member.query.repository.DepartmentQueryRepository;

class DepartmentQueryServiceTest {

  @Mock private DepartmentQueryRepository departmentQueryRepository;

  @InjectMocks private DepartmentQueryService departmentQueryService;

  @BeforeEach
  void setUp() {
    openMocks(this);
  }

  @Test
  @DisplayName("부서 목록 조회 - 성공")
  void getDepartments_success() {
    // given
    List<DepartmentResponse> mockResult =
        List.of(
            new DepartmentResponse("개발팀"),
            new DepartmentResponse("기획팀"),
            new DepartmentResponse("디자인팀"));

    when(departmentQueryRepository.findAllDepartments()).thenReturn(mockResult);

    // when
    List<DepartmentResponse> result = departmentQueryService.getDepartments();

    // then
    assertThat(result).hasSize(3);
    assertThat(result).extracting("departmentName").containsExactly("개발팀", "기획팀", "디자인팀");
  }
}
