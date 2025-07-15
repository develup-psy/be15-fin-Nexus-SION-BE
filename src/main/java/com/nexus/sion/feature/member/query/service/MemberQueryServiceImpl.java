package com.nexus.sion.feature.member.query.service;

import static com.example.jooq.generated.tables.Member.MEMBER;

import java.util.List;

import org.jooq.Condition;
import org.jooq.SortField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.generated.enums.MemberGradeCode;
import com.example.jooq.generated.enums.MemberRole;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.*;
import com.nexus.sion.feature.member.query.repository.MemberQueryRepository;
import com.nexus.sion.feature.member.query.util.MemberConditionBuilder;
import com.nexus.sion.feature.member.query.util.SortFieldSelector;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemberQueryServiceImpl implements MemberQueryService {

  private final MemberQueryRepository memberQueryRepository;
  private final MemberConditionBuilder memberConditionBuilder;
  private final SortFieldSelector sortFieldSelector;

  @Override
  public PageResponse<MemberListResponse> getAllMembers(MemberListRequest request) {
    int page = request.getPage();
    int size = request.getSize();
    String sortBy = request.getSortBy() != null ? request.getSortBy() : "employeeName";
    String sortDir = request.getSortDir() != null ? request.getSortDir() : "asc";

    // 기본 조건
    Condition condition = MEMBER.DELETED_AT.isNull().and(MEMBER.ROLE.ne(MemberRole.ADMIN));

    // 상태 필터
    if (request.getStatus() != null) {
      try {
        condition =
            condition.and(
                MEMBER.STATUS.eq(MemberStatus.valueOf(request.getStatus().toUpperCase())));
      } catch (IllegalArgumentException e) {
        throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
      }
    }

    // 등급 필터
    if (request.getGradeCode() != null) {
      try {
        condition =
            condition.and(
                MEMBER.GRADE_CODE.eq(
                    MemberGradeCode.valueOf(request.getGradeCode().toUpperCase())));
      } catch (IllegalArgumentException e) {
        throw new BusinessException(ErrorCode.INVALID_GRADE);
      }
    }

    // role 필터
    if (request.getRole() != null) {
      try {
        condition =
            condition.and(MEMBER.ROLE.eq(MemberRole.valueOf(request.getRole().toUpperCase())));
      } catch (IllegalArgumentException e) {
        throw new BusinessException(ErrorCode.INVALID_MEMBER_ROLE);
      }
    }

    // 정렬 필드
    SortField<?> sortField =
        switch (sortBy) {
          case "employeeId" ->
              "desc".equalsIgnoreCase(sortDir)
                  ? MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.desc()
                  : MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.asc();
          case "joinedAt" ->
              "desc".equalsIgnoreCase(sortDir) ? MEMBER.JOINED_AT.desc() : MEMBER.JOINED_AT.asc();
          default ->
              "desc".equalsIgnoreCase(sortDir)
                  ? MEMBER.EMPLOYEE_NAME.desc()
                  : MEMBER.EMPLOYEE_NAME.asc();
        };

    Condition fullCondition = condition;
    String keyword = request.getKeyword();
    if (keyword != null && !keyword.isBlank()) {
      fullCondition =
          fullCondition.and(
              MEMBER
                  .EMPLOYEE_IDENTIFICATION_NUMBER
                  .containsIgnoreCase(keyword)
                  .or(MEMBER.EMPLOYEE_NAME.containsIgnoreCase(keyword)));
    }

    long total = memberQueryRepository.countMembers(fullCondition);
    var content = memberQueryRepository.findAllMembers(request, fullCondition, sortField);
    return PageResponse.fromJooq(content, total, page, size);
  }

  @Transactional(readOnly = true)
  public PageResponse<MemberListResponse> searchMembers(String keyword, int page, int size) {
    int offset = page * size;
    List<MemberListResponse> content = memberQueryRepository.searchMembers(keyword, offset, size);
    int total = memberQueryRepository.countSearchMembers(keyword);
    return PageResponse.fromJooq(content, total, page, size);
  }

  @Transactional(readOnly = true)
  public PageResponse<AdminSearchResponse> searchAdmins(String keyword, int page, int size) {
    int offset = page * size;
    List<AdminSearchResponse> content = memberQueryRepository.searchAdmins(keyword, offset, size);
    int total = memberQueryRepository.countSearchAdmins(keyword);
    return PageResponse.fromJooq(content, total, page, size);
  }

  public MemberDetailResponse getMemberDetail(String employeeId) {
    return memberQueryRepository
        .findByEmployeeId(employeeId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  @Override
  public PageResponse<MemberSquadListResponse> squadSearchMembers(MemberListQuery query) {
    // 조건 및 정렬 분리 위임
    SortField<?> sortField = sortFieldSelector.select(query.sortBy(), query.sortDir());
    Condition condition = memberConditionBuilder.build(query);

    long total = memberQueryRepository.countMembers(condition);
    List<MemberSquadListResponse> content =
        memberQueryRepository.findAllSquadMembers(query, condition, sortField);

    return PageResponse.fromJooq(content, total, query.page(), query.size());
  }

  @Override
  public List<ScoreTrendDto> getMonthlyTotalScoreTrend(String employeeId) {
    return memberQueryRepository.findMonthlyTotalScoreTrend(employeeId);
  }

  @Override
  public List<ScoreTrendDto> getMonthlyTechStackScoreTrend(String employeeId) {
    return memberQueryRepository.findMonthlyTechStackScoreTrend(employeeId);
  }

  @Override
  public DashboardSummaryResponse getDashboardSummary() {
    return new DashboardSummaryResponse(
        memberQueryRepository.findPendingProjects(),
        memberQueryRepository.findAnalyzingProjects(),
        memberQueryRepository.fetchTopDevelopers(),
        memberQueryRepository.fetchTopFreelancers(),
        memberQueryRepository.fetchDeveloperAvailability(),
        memberQueryRepository.fetchTopTechStacks());
  }

  @Override
  public String getMyProfileImage(String employeeId) {
    return memberQueryRepository
        .findProfileImageUrlById(employeeId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }
}
