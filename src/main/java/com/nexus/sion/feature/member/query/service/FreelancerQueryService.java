package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.response.FreelancerDetailResponse;
import com.nexus.sion.feature.member.query.dto.response.FreelancerListResponse;

public interface FreelancerQueryService {
  PageResponse<FreelancerListResponse> getFreelancers(int page, int size);

  FreelancerDetailResponse getFreelancerDetail(String freelancerId);
}
