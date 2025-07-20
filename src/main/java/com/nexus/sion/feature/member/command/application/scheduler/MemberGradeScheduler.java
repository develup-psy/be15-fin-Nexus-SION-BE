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

  @Scheduled(cron = "0 0 3 1 * *") // 매월 1일 새벽 3시
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

    // ✅ 2-1. 등급 변경 대상자 ID 목록 추출
    List<String> employeeIds = scoredMembers.stream().map(ScoredMember::employeeId).toList();

    // ✅ 2-2. 모든 사용자 이름과 현재 등급 한 번에 조회
    Map<String, String> usernameMap =
        dsl.select(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER, MEMBER.EMPLOYEE_NAME)
            .from(MEMBER)
            .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.in(employeeIds))
            .fetchMap(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER, MEMBER.EMPLOYEE_NAME);

    Map<String, MemberGradeCode> previousGradeMap =
        dsl.select(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER, MEMBER.GRADE_CODE)
            .from(MEMBER)
            .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.in(employeeIds))
            .fetchMap(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER, MEMBER.GRADE_CODE);

    // 3. 등급 산정 및 알림
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
      MemberGradeCode previousGrade = previousGradeMap.get(m.employeeId());

      // 변경된 경우에만 업데이트 및 알림
      if (!newGrade.equals(previousGrade)) {
        dsl.update(MEMBER)
            .set(MEMBER.GRADE_CODE, newGrade)
            .where(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.eq(m.employeeId()))
            .execute();

        String username = usernameMap.get(m.employeeId());

        String message =
            NotificationType.GRADE_CHANGE.generateMessage(
                username, previousGrade.name(), newGrade.name());

        notificationCommandService.createAndSendNotification(
            null, m.employeeId(), message, NotificationType.GRADE_CHANGE, null);
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
