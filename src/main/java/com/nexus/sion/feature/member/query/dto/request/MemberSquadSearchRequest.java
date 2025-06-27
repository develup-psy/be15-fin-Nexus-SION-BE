package com.nexus.sion.feature.member.query.dto.request;

import com.example.jooq.generated.enums.GradeGradeCode;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.query.dto.internal.MemberListQuery;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@NoArgsConstructor
@Slf4j
@ToString
@Setter
public class MemberSquadSearchRequest {

    private String status;             // Optional, 검증 필요
    private List<String> grade;        // Optional
    private List<String> stacks;       // Optional

    private String sortBy = "employeeName";
    private String sortDir = "asc";

    private int page = 0;
    private int size = 10;

    public MemberListQuery toQuery() {
        // status 유효성 검사 및 파싱
        MemberStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = MemberStatus.valueOf(this.status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
            }
        }

        // Grade 파싱
        List<GradeGradeCode> parsedGrades = null;
        if (this.grade != null && !this.grade.isEmpty()) {
            try {
                parsedGrades = this.grade.stream()
                        .map(s -> GradeGradeCode.valueOf(s.toUpperCase()))
                        .toList();
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.TECH_STACK_NOT_FOUND);
            }
        }

        return new MemberListQuery(
                parsedStatus,
                parsedGrades,
                this.stacks,
                this.sortBy,
                this.sortDir,
                this.page,
                this.size
        );
    }
}
