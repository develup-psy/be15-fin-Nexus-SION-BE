package com.nexus.sion.feature.member.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexus.sion.feature.member.query.dto.response.DepartmentResponse;
import com.nexus.sion.feature.member.query.repository.DepartmentQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentQueryService {

  private final DepartmentQueryRepository departmentQueryRepository;

  public List<DepartmentResponse> getDepartments() {
    return departmentQueryRepository.findAllDepartments();
  }
}
