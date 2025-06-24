package com.nexus.sion.feature.member.query.dto.response;

import java.time.LocalDateTime;

public record DepartmentResponse(
        String departmentName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}