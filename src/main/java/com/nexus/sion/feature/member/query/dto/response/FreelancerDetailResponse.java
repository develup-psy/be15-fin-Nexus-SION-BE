package com.nexus.sion.feature.member.query.dto.response;

import java.time.LocalDate;

public record FreelancerDetailResponse(
    String freelancerId,
    String name,
    String phoneNumber,
    String email,
    Integer careerYears,
    String resumeUrl,
    String profileImageUrl,
    LocalDate birthday) {}
