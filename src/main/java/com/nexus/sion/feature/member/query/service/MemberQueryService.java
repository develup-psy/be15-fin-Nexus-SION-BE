package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.request.MemberSquadSearchRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberDetailResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;
import com.nexus.sion.feature.member.query.dto.response.MemberSquadListResponse;

public interface MemberQueryService {
  PageResponse<MemberListResponse> getAllMembers(MemberListRequest request);

  PageResponse<MemberListResponse> searchMembers(String keyword, int page, int size);

  MemberDetailResponse getMemberDetail(String employeeId);

  PageResponse<MemberSquadListResponse> squadSearchMembers(MemberListQuery request);
}
