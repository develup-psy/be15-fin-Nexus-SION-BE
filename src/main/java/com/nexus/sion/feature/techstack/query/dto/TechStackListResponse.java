package com.nexus.sion.feature.techstack.query.dto;

import java.time.LocalDateTime;

public record TechStackListResponse(
        String techStackName,
        LocalDateTime created_at
) {
}
