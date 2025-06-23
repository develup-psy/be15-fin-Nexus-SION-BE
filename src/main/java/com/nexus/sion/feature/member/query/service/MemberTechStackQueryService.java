package com.nexus.sion.feature.member.query.service;

import java.util.List;

import com.nexus.sion.feature.member.query.dto.response.MemberTechStackResponse;

public interface MemberTechStackQueryService {
  List<MemberTechStackResponse> getTechStacks(String employeeId);
}
