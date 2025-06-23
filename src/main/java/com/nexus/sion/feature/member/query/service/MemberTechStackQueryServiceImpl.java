package com.nexus.sion.feature.member.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.query.dto.response.MemberTechStackResponse;
import com.nexus.sion.feature.member.query.repository.MemberTechStackQueryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemberTechStackQueryServiceImpl implements MemberTechStackQueryService {

  private final MemberTechStackQueryRepository memberTechStackQueryRepository;

  public List<MemberTechStackResponse> getTechStacks(String employeeId) {
    return memberTechStackQueryRepository.findTechStacksByEmployeeId(employeeId);
  }
}
