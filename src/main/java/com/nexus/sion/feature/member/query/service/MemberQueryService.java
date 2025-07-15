package com.nexus.sion.feature.member.query.service;

import java.util.List;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.*;

public interface MemberQueryService {
  PageResponse<MemberListResponse> getAllMembers(MemberListRequest request);

  PageResponse<MemberListResponse> searchMembers(String keyword, int page, int size);

  PageResponse<AdminSearchResponse> searchAdmins(String keyword, int page, int size);

  MemberDetailResponse getMemberDetail(String employeeId);

  PageResponse<MemberSquadListResponse> squadSearchMembers(MemberListQuery request);

  List<ScoreTrendDto> getMonthlyTotalScoreTrend(String employeeId);

  List<ScoreTrendDto> getMonthlyTechStackScoreTrend(String employeeId);

  DashboardSummaryResponse getDashboardSummary();

  String getMyProfileImage(String employeeId);
}
