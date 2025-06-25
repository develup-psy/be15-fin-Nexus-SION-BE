package com.nexus.sion.feature.member.command.application.dto.request;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;

public record MemberStatusUpdateRequest(MemberStatus status) {}