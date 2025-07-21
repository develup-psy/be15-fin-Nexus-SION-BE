package com.nexus.sion.feature.member.query.service;

import static com.example.jooq.generated.tables.Member.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nexus.sion.feature.member.query.dto.response.*;
import org.jooq.Condition;
import org.jooq.SortField;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.jooq.generated.enums.GradeGradeCode;
import com.example.jooq.generated.enums.MemberRole;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.repository.MemberQueryRepository;
import com.nexus.sion.feature.member.query.util.MemberConditionBuilder;
import com.nexus.sion.feature.member.query.util.SortFieldSelector;
import com.nexus.sion.feature.member.query.dto.response.DashboardSummaryResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberQueryService 단위 테스트")
class MemberQueryServiceImplTest {

  @InjectMocks private MemberQueryServiceImpl memberQueryService;

  @Mock private MemberQueryRepository memberQueryRepository;

  @Mock private MemberConditionBuilder memberConditionBuilder;

  @Mock private SortFieldSelector sortFieldSelector;

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
                "department1",
                "position1",
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
                "department1",
                "position1",
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
            LocalDate.of(2022, 1, 1),
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

  @Nested
  @DisplayName("squadSearchMembers() - 개발자 조회")
  class SquadSearchTests {

    private MemberListQuery baseQuery;
    private Condition baseCondition;
    private SortField<?> baseSortField;

    @BeforeEach
    void setUp() {

      baseQuery =
          new MemberListQuery(
              "키워드",
              MemberStatus.AVAILABLE,
              List.of(GradeGradeCode.S),
              List.of("Java", "Spring"),
              "employeeName",
              "asc",
              1,
              10,
              new ArrayList<>(List.of("INSIDER")));

      baseCondition = MEMBER.ROLE.isNotNull(); // 단순한 조건으로 설정
      baseSortField = MEMBER.EMPLOYEE_NAME.asc();
    }

//    @Test
//    @DisplayName("성공: 상태 + 등급 + 기술스택 조건으로 정상 조회")
//    void givenValidFilters_whenSearch_thenReturnsPagedResult() {
//      // given
//      List<MemberSquadListResponse> mockResults =
//          List.of(new MemberSquadListResponse("EMP001", "홍길동", "S", "AVAILABLE", "Java", 0, null));
//
//      when(memberConditionBuilder.build(baseQuery)).thenReturn(baseCondition);
//      when(sortFieldSelector.select(eq("employeeName"), eq("asc")))
//          .thenAnswer(invocation -> baseSortField);
//      when(memberQueryRepository.countMembers(baseCondition)).thenReturn(1L);
//      when(memberQueryRepository.findAllSquadMembers(baseQuery, baseCondition, baseSortField))
//          .thenReturn(mockResults);
//
//      // when
//      PageResponse<MemberSquadListResponse> result =
//          memberQueryService.squadSearchMembers(baseQuery);
//
//      // then
//      assertThat(result.getTotalElements()).isEqualTo(1L);
//      assertThat(result.getContent()).hasSize(1);
//      assertThat(result.getContent().get(0).name()).isEqualTo("홍길동");
//      assertThat(result.getContent().get(0).grade()).isEqualTo("S");
//      assertThat(result.getContent().get(0).topTechStackName()).isEqualTo("Java");
//
//      // verify
//      verify(memberConditionBuilder).build(baseQuery);
//      verify(sortFieldSelector).select("employeeName", "asc");
//      verify(memberQueryRepository).countMembers(baseCondition);
//      verify(memberQueryRepository).findAllSquadMembers(baseQuery, baseCondition, baseSortField);
//      verifyNoMoreInteractions(memberConditionBuilder, sortFieldSelector, memberQueryRepository);
//    }

    @Test
    @DisplayName("경계값: 조건과 일치하는 개발자가 없으면 빈 리스트 반환")
    void givenFilters_whenNoMatchingMembers_thenReturnsEmptyList() {
      // given
      MemberListQuery noMatchQuery =
          new MemberListQuery(
              "존재하지않는키워드",
              MemberStatus.AVAILABLE,
              List.of(GradeGradeCode.S),
              List.of("UnknownStack"),
              "employeeName",
              "asc",
              1,
              10,
              List.of("INSIDER"));

      when(memberConditionBuilder.build(noMatchQuery)).thenReturn(baseCondition);
      when(sortFieldSelector.select(eq("employeeName"), eq("asc")))
          .thenAnswer(invocation -> baseSortField);
      when(memberQueryRepository.countMembers(baseCondition)).thenReturn(0L);
      when(memberQueryRepository.findAllSquadMembers(noMatchQuery, baseCondition, baseSortField))
          .thenReturn(List.of());

      // when
      PageResponse<MemberSquadListResponse> result =
          memberQueryService.squadSearchMembers(noMatchQuery);

      // then
      assertThat(result.getTotalElements()).isEqualTo(0L);
      assertThat(result.getContent()).isEmpty();

      // verify
      verify(memberConditionBuilder).build(noMatchQuery);
      verify(sortFieldSelector).select("employeeName", "asc");
      verify(memberQueryRepository).countMembers(baseCondition);
      verify(memberQueryRepository).findAllSquadMembers(noMatchQuery, baseCondition, baseSortField);
      verifyNoMoreInteractions(memberConditionBuilder, sortFieldSelector, memberQueryRepository);
    }

//    @Test
//    @DisplayName("성공: 필터 없이 전체 조회 요청 시 전체 목록 반환")
//    void givenNoFilter_whenSearch_thenReturnsAllMembers() {
//      // given
//      MemberListQuery noFilterQuery =
//          new MemberListQuery(
//              null, null, null, null, "employeeName", "asc", 1, 10, List.of("INSIDER"));
//
//      when(memberConditionBuilder.build(noFilterQuery)).thenReturn(baseCondition);
//      when(sortFieldSelector.select(eq("employeeName"), eq("asc")))
//          .thenAnswer(invocation -> baseSortField);
//
//      List<MemberSquadListResponse> results =
//          List.of(
//              new MemberSquadListResponse("EMP001", "홍길동", null, "AVAILABLE", "Spring", 0, null));
//
//      when(memberQueryRepository.countMembers(baseCondition)).thenReturn(1L);
//      when(memberQueryRepository.findAllSquadMembers(noFilterQuery, baseCondition, baseSortField))
//          .thenReturn(results);
//
//      // when
//      PageResponse<MemberSquadListResponse> result =
//          memberQueryService.squadSearchMembers(noFilterQuery);
//
//      // then
//      assertThat(result.getContent()).hasSize(1);
//      assertThat(result.getContent().get(0).name()).isEqualTo("홍길동");
//
//      // verify
//      verify(memberConditionBuilder).build(noFilterQuery);
//      verify(sortFieldSelector).select("employeeName", "asc");
//      verify(memberQueryRepository).countMembers(baseCondition);
//      verify(memberQueryRepository)
//          .findAllSquadMembers(noFilterQuery, baseCondition, baseSortField);
//      verifyNoMoreInteractions(memberConditionBuilder, sortFieldSelector, memberQueryRepository);
//    }

    @Test
    @DisplayName("예외: 정렬 기준이 유효하지 않은 경우 예외 발생")
    void givenInvalidSortBy_whenSearch_thenThrowsException() {
      // given
      MemberListQuery badSortQuery =
          new MemberListQuery(
              null, null, null, null, "invalidField", "asc", 1, 10, List.of("INSIDER"));

      when(sortFieldSelector.select(eq("invalidField"), eq("asc")))
          .thenThrow(new BusinessException(ErrorCode.INVALID_SORT_COLUMN));

      // when & then
      assertThrows(
          BusinessException.class,
          () -> {
            memberQueryService.squadSearchMembers(badSortQuery);
          });

      // verify
      verify(sortFieldSelector).select(eq("invalidField"), eq("asc"));
      verifyNoInteractions(memberConditionBuilder, memberQueryRepository);
    }

    @DisplayName("사번으로 프로필 이미지 URL 조회 성공")
    @Test
    void getMyProfileImage_success() {
      // given
      String employeeId = "DEV001";
      String expectedImageUrl = "https://sion-bucket.s3.amazonaws.com/profile/abcd.png";
      when(memberQueryRepository.findProfileImageUrlById(employeeId))
          .thenReturn(Optional.of(expectedImageUrl));

      // when
      String actualImageUrl = memberQueryService.getMyProfileImage(employeeId);

      // then
      assertThat(actualImageUrl).isEqualTo(expectedImageUrl);
      verify(memberQueryRepository, times(1)).findProfileImageUrlById(employeeId);
    }
  }

  @DisplayName("유효하지 않은 등급 필터는 예외를 발생시킨다")
  @Test
  void getAllMembers_invalidGradeCode_throwsException() {
    MemberListRequest request = MemberListRequest.builder().gradeCode("INVALID").build();

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> memberQueryService.getAllMembers(request)
    );

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_GRADE);
  }

  @DisplayName("유효하지 않은 역할 필터는 예외를 발생시킨다")
  @Test
  void getAllMembers_invalidRole_throwsException() {
    MemberListRequest request = MemberListRequest.builder().role("INVALID_ROLE").build();

    BusinessException exception = assertThrows(
            BusinessException.class,
            () -> memberQueryService.getAllMembers(request)
    );

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_MEMBER_ROLE);
  }

  @DisplayName("키워드로 관리자 검색 결과 반환")
  @Test
  void searchAdmins_returnMatchingResults() {
    String keyword = "관리자";
    int page = 0;
    int size = 5;
    int offset = page * size;

    List<AdminSearchResponse> mockResults = List.of(
            new AdminSearchResponse("ADMIN001", "관리자", "profile.jpg")
    );

    when(memberQueryRepository.searchAdmins(keyword, offset, size)).thenReturn(mockResults);
    when(memberQueryRepository.countSearchAdmins(keyword)).thenReturn(1);

    PageResponse<AdminSearchResponse> result = memberQueryService.searchAdmins(keyword, page, size);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("관리자");
  }

  @DisplayName("총 점수 트렌드 조회")
  @Test
  void getMonthlyTotalScoreTrend_success() {
    String employeeId = "DEV001";
    List<ScoreTrendDto> mockTrend = List.of(new ScoreTrendDto("2025-06", "Java",80));

    when(memberQueryRepository.findMonthlyTotalScoreTrend(employeeId)).thenReturn(mockTrend);

    List<ScoreTrendDto> result = memberQueryService.getMonthlyTotalScoreTrend(employeeId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).score()).isEqualTo(80);
  }

  @DisplayName("기술스택 점수 트렌드 조회")
  @Test
  void getMonthlyTechStackScoreTrend_success() {
    String employeeId = "DEV001";
    List<ScoreTrendDto> mockTrend = List.of(new ScoreTrendDto("2025-06", "React",90));

    when(memberQueryRepository.findMonthlyTechStackScoreTrend(employeeId)).thenReturn(mockTrend);

    List<ScoreTrendDto> result = memberQueryService.getMonthlyTechStackScoreTrend(employeeId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).score()).isEqualTo(90);
  }
}
