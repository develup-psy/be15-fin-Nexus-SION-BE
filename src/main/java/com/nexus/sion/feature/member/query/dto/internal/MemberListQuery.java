package com.nexus.sion.feature.member.query.dto.internal;

import com.example.jooq.generated.enums.GradeGradeCode;
import com.example.jooq.generated.enums.MemberStatus;

import java.util.List;

public record MemberListQuery(
        MemberStatus status,
        List<GradeGradeCode> grades,
        List<String> techStacks,
        String sortBy,
        String sortDir,
        int page,
        int size
) {}

