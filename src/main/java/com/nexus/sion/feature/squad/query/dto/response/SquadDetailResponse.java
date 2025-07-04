package com.nexus.sion.feature.squad.query.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class SquadDetailResponse {
  private String squadCode;
  private String squadName;
  private boolean aiRecommended;
  private String estimatedPeriod;
  private String estimatedCost;
  private SummaryInfo summary;
  private List<String> techStacks;
  private List<MemberInfo> members; // 구성원 정보
  private List<CostBreakdown> costDetails;
  private String recommendationReason;
  private List<CommentResponse> comments;

  @Getter
  @AllArgsConstructor
  public static class MemberInfo {
    private boolean isLeader; // 리더 여부
    private String imageUrl; // 프로필 사진 URL
    private String job; // 직무
    private String name; // 이름
  }

  @Getter
  @AllArgsConstructor
  public static class CostBreakdown {
    private String name; // 구성원 이름
    private String job; // 직무
    private String grade; // 등급
    private String cost; // 단가 (예: ₩2,000,000)
  }

  @Getter
  @AllArgsConstructor
  public static class SummaryInfo {
    private Map<String, Long> jobCounts;
    private Map<String, Long> gradeCounts;
  }

  @Getter
  @AllArgsConstructor
  public static class CommentResponse {
    private Long id;
    private String employeeIdentificationNumber;
    private String content;
    private LocalDateTime createdAt;
  }
}
