package com.nexus.sion.feature.squad.query.dto.response;

import com.example.jooq.generated.enums.SquadOriginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadDetailResponse {
    private String squadCode;
    private String title;
    private String recommendationReason;
    private int totalMemberCount;
    private Map<String, Integer> memberCountByJob;
    private Map<String, Integer> gradeCount;
    private List<String> techStacks;
    private int estimatedDuration;
    private int totalCost;
    private List<MemberDetail> members;
    private String description;
    private SquadOriginType origin;
    private Boolean isActive;

    @Getter
    @Builder
    public static class MemberDetail {
        private String name;
        private String job;
        private String grade;
        private int monthlyUnitPrice;
        private String memberId;
        private BigDecimal productivity;
    }
}
