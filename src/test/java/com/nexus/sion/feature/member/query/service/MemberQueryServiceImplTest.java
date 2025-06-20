package com.nexus.sion.feature.member.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.nexus.sion.feature.member.query.dto.response.MemberDetailResponse;
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
    assertThrows(
        BusinessException.class,
        () -> memberQueryService.getAllMembers(request),
        ErrorCode.INVALID_MEMBER_STATUS.getMessage());
  }

  @DisplayName("검색 결과 반환")
  @Test
  void searchAvailableMembers_returnMatchingResults() {
    // given
    String keyword = "홍";
    int page = 0;
    int size = 5;
    int offset = page * size;

    List<MemberListResponse> mockResults =
        List.of(
            new MemberListResponse(
                "EMP001",
                "홍길동",
                "01012345678",
                "hong@example.com",
                "INSIDER",
                "A",
                "AVAILABLE",
                null,
                null,
                "Java",
                3));

    when(memberQueryRepository.searchMembers(keyword, offset, size)).thenReturn(mockResults);
    when(memberQueryRepository.countSearchMembers(keyword)).thenReturn(1);

    // when
    PageResponse<MemberListResponse> result = memberQueryService.searchMembers(keyword, page, size);

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("홍길동");

    verify(memberQueryRepository, times(1)).searchMembers(keyword, offset, size);
    verify(memberQueryRepository, times(1)).countSearchMembers(keyword);
  }

  @DisplayName("사번으로 회원 상세 조회 성공")
  @Test
  void getMemberDetail_success() {
    // given
    String employeeId = "DEV001";
    MemberDetailResponse mockResponse =
        new MemberDetailResponse(
            employeeId,
            "홍길동",
            "profile.jpg",
            "01012345678",
            "백엔드 개발자",
            "플랫폼팀",
            LocalDate.of(1998, 4, 15),
            LocalDateTime.of(2022, 1, 1, 0, 0),
            "hong@example.com",
            3,
            55000000L,
            "AVAILABLE",
            "A",
            "INSIDER");

    when(memberQueryRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(mockResponse));

    // when
    MemberDetailResponse result = memberQueryService.getMemberDetail(employeeId);

    // then
    assertThat(result.employeeId()).isEqualTo(employeeId);
    assertThat(result.name()).isEqualTo("홍길동");
    assertThat(result.email()).isEqualTo("hong@example.com");

    verify(memberQueryRepository, times(1)).findByEmployeeId(employeeId);
  }

  @DisplayName("사번으로 회원 상세 조회 시 존재하지 않으면 BusinessException 발생")
  @Test
  void getMemberDetail_notFound_throwsException() {
    // given
    String employeeId = "UNKNOWN001";
    when(memberQueryRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

    // when & then
    BusinessException exception =
        assertThrows(BusinessException.class, () -> memberQueryService.getMemberDetail(employeeId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    verify(memberQueryRepository, times(1)).findByEmployeeId(employeeId);
  }
}
