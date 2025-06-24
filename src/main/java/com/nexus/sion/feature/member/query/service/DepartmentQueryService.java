package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.query.dto.response.DepartmentResponse;
import com.nexus.sion.feature.member.query.repository.DepartmentQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentQueryService {

    private final DepartmentQueryRepository departmentQueryRepository;

    public List<DepartmentResponse> getDepartments() {
        return departmentQueryRepository.findAllDepartments();
    }
}