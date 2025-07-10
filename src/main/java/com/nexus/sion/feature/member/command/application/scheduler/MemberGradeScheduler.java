package com.nexus.sion.feature.member.command.application.scheduler;

import static com.example.jooq.generated.Tables.GRADE;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.MemberScoreHistory.MEMBER_SCORE_HISTORY;

import java.util.*;

import org.jooq.DSLContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.jooq.generated.enums.GradeGradeCode;
import com.example.jooq.generated.enums.MemberGradeCode;
import com.example.jooq.generated.tables.records.MemberScoreHistoryRecord;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberGradeScheduler {

  private final DSLContext dsl;
  private final NotificationCommandService notificationCommandService;

  @Scheduled(cron = "0 0 3 1 * *") // 매일 새벽 3시 실행
  public void updateDeveloperGrades() {
    // 1. 최신 점수 가져오기
    List<MemberScoreHistoryRecord> latestScores =
        dsl.selectFrom(MEMBER_SCORE_HISTORY)
            .where(
                MEMBER_SCORE_HISTORY.CREATED_AT.in(
                    dsl.select(MEMBER_SCORE_HISTORY.CREATED_AT.max())
                        .from(MEMBER_SCORE_HISTORY)
                        .groupBy(MEMBER_SCORE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER)))
            .fetch();

    // 2. 점수 합산 및 정렬
    List<ScoredMember> scoredMembers =
        latestScores.stream()
            .map(
                r ->
                    new ScoredMember(
                        r.getEmployeeIdentificationNumber(),
                        r.getTotalTechStackScores() + r.getTotalCertificateScores()))
            .sorted(Comparator.comparingInt(ScoredMember::totalScore).reversed())
            .toList();

    int size = scoredMembers.size();
    int sCut = (int) Math.ceil(size * 0.2);
    int aCut = (int) Math.ceil(size * 0.4);
    int bCut = (int) Math.ceil(size * 0.6);
    int cCut = (int) Math.ceil(size * 0.8);

    Map<String, List<ScoredMember>> gradeGroups = new HashMap<>();

    // 3. member 테이블 업데이트 및 등급별 리스트 구성
    for (int i = 0; i < size; i++) {
      ScoredMember m = scoredMembers.get(i);
      String gradeStr;
      if (m.totalScore() == 0) {
        gradeStr = "D";
      } else if (i < sCut) {
        gradeStr = "S";
      } else if (i < aCut) {
        gradeStr = "A";
      } else if (i < bCut) {
        gradeStr = "B";
      } else if (i < cCut) {
        gradeStr = "C";
      } else {
        gradeStr = "D";
      }

      MemberGradeCode newGrade = MemberGradeCode.valueOf(gradeStr);

      // 현재 등급 조회
      MemberGradeCode previousGrade =
          dsl.select(MEMBER.GRADE_CODE)
              .from(MEMBER)
              .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(m.employeeId()))
              .fetchOneInto(MemberGradeCode.class);

      // 등급이 변경된 경우에만 업데이트 및 알림
      if (!newGrade.equals(previousGrade)) {
        dsl.update(MEMBER)
            .set(MEMBER.GRADE_CODE, newGrade)
            .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(m.employeeId()))
            .execute();

        // 알림 발송
        notificationCommandService.createAndSendNotification(
            null,
            m.employeeId(),
            String.format("당신의 등급이 %s에서 %s 등급으로 변경되었습니다.", previousGrade, newGrade),
            NotificationType.GRADE_CHANGE,
            null);
      }

      gradeGroups.computeIfAbsent(gradeStr, k -> new ArrayList<>()).add(m);
    }

    // 4. grade 테이블 최소 점수 업데이트
    for (Map.Entry<String, List<ScoredMember>> entry : gradeGroups.entrySet()) {
      int minScore = entry.getValue().stream().mapToInt(ScoredMember::totalScore).min().orElse(0);

      dsl.update(GRADE)
          .set(GRADE.SCORE_THRESHOLD, minScore)
          .where(GRADE.GRADE_CODE.eq(GradeGradeCode.valueOf(entry.getKey())))
          .execute();
    }

    log.info("개발자 등급 산정 스케줄러 완료 - 대상: {}명", size);
  }

  private record ScoredMember(String employeeId, int totalScore) {}
}
