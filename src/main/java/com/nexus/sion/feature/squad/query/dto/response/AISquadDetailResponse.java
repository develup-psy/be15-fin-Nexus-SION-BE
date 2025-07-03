package com.nexus.sion.feature.squad.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AISquadDetailResponse {
    private String squadCode;
    private String title;
    private String recommendationCriteria;
    private String recommendationReason;
    private int totalMemberCount;
    private Map<String, Integer> memberCountByJob;
    private Map<String, Integer> gradeCount;
    private List<String> techStacks;
    private int estimatedDuration;
    private int totalCost;
    private List<MemberDetail> members;
    private List<CommentDetail> comments;

    @Getter
    @Builder
    public static class MemberDetail {
        private String name;
        private String job;
        private String grade;
        private int monthlyUnitPrice;
    }

    @Getter
    @Builder
    public static class CommentDetail {
        private String author;
        private LocalDate date;
        private String content;
    }
}
