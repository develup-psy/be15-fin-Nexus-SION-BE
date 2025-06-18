package com.nexus.sion.feature.member.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.jooq.Condition;
import org.jooq.SortField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.example.jooq.generated.enums.MemberRole;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;
import com.nexus.sion.feature.member.query.repository.MemberQueryRepository;

class MemberQueryServiceImplTest {

  @InjectMocks private MemberQueryServiceImpl memberQueryService;

  @Mock private MemberQueryRepository memberQueryRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @DisplayName("올바른 요청에 대해 필터 및 정렬 조건이 적용된 회원 목록을 반환한다")
  @Test
  void getAllMembers_success() {
    // given
    MemberListRequest request =
        MemberListRequest.builder()
            .page(0)
            .size(10)
            .status("AVAILABLE")
            .sortBy("employeeName")
            .sortDir("asc")
            .build();

    List<MemberListResponse> dummyList =
        List.of(
            new MemberListResponse(
                "DEV001",
                "홍길동",
                "01012345678",
                "test@a.com",
                MemberRole.INSIDER.name(),
                "A",
                MemberStatus.AVAILABLE.name(),
                "profile.jpg",
                null,
                "Java",
                5));

    when(memberQueryRepository.countMembers(any(Condition.class))).thenReturn(1L);
    when(memberQueryRepository.findAllMembers(
            eq(request), any(Condition.class), any(SortField.class)))
        .thenReturn(dummyList);

    // when
    PageResponse<MemberListResponse> result = memberQueryService.getAllMembers(request);

    // then
    assertThat(result.getTotalElements()).isEqualTo(1L);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("홍길동");

    verify(memberQueryRepository, times(1)).countMembers(any());
    verify(memberQueryRepository, times(1)).findAllMembers(any(), any(), any());
  }

  @DisplayName("유효하지 않은 상태 필터는 예외를 발생시킨다")
  @Test
  void getAllMembers_invalidStatus_throwsException() {
    // given
    MemberListRequest request = MemberListRequest.builder().status("INVALID_STATUS").build();

    // when & then
    org.junit.jupiter.api.Assertions.assertThrows(
        BusinessException.class,
        () -> memberQueryService.getAllMembers(request),
        ErrorCode.INVALID_MEMBER_STATUS.getMessage());
  }
}
