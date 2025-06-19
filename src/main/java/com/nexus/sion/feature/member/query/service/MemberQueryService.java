package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;

public interface MemberQueryService {
  PageResponse<MemberListResponse> getAllMembers(MemberListRequest request);

  PageResponse<MemberListResponse> searchMembers(String keyword, int page, int size);
}
