package com.nexus.sion.feature.member.query.dto.response;

import java.time.LocalDateTime;

public record FreelancerListResponse(
    String freelancerId,
    String name,
    String profileImageUrl,
    String email,
    LocalDateTime createdAt) {}
