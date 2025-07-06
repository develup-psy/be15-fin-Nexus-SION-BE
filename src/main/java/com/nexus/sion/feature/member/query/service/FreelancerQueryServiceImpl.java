package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.FreelancerDetailResponse;
import org.springframework.stereotype.Service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.response.FreelancerListResponse;
import com.nexus.sion.feature.member.query.repository.FreelancerQueryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FreelancerQueryServiceImpl implements FreelancerQueryService {

  private final FreelancerQueryRepository freelancerQueryRepository;

  @Override
  public PageResponse<FreelancerListResponse> getFreelancers(int page, int size) {
    return freelancerQueryRepository.getFreelancerList(page, size);
  }

  @Override
  public FreelancerDetailResponse getFreelancerDetail(String freelancerId) {
    return freelancerQueryRepository.getFreelancerDetail(freelancerId);
  }
}
