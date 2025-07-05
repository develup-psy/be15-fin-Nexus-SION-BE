package com.nexus.sion.feature.member.query.dto.internal;

import java.util.List;

import com.example.jooq.generated.enums.GradeGradeCode;
import com.example.jooq.generated.enums.MemberStatus;

public record MemberListQuery(
    String keyword,
    MemberStatus status,
    List<GradeGradeCode> grades,
    List<String> techStacks,
    String sortBy,
    String sortDir,
    int page,
    int size,
    List<String> memberRoles) {}
