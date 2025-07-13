package com.nexus.sion.feature.notification.command.domain.aggregate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum NotificationType {

  /** 업무 업로드 요청 알림 */
  TASK_UPLOAD_REQUEST("프로젝트가 종료되었습니다. 진행하신 프로젝트의 이력을 등록해주세요."),

  /** FP 분석 완료 알림 */
  FP_ANALYSIS_COMPLETE("FP 분석이 완료되었습니다. 결과를 확인해주세요."),

  /** FP 분석 실패 알림 */
  FP_ANALYSIS_FAILURE("FP 분석 처리에 실패하였습니다. 다시 시도해주세요."),

  /** 스쿼드 댓글 알림 */
  SQUAD_COMMENT("{username}님이 스쿼드에 댓글을 작성했습니다."),

  /** 스쿼드 공유 알림 */
  SQUAD_SHARE("{username}님이 스쿼드를 공유했습니다."),

  /** 등급 변경 알림 */
  GRADE_CHANGE("등급이 변경되었습니다."),

  /** 프로젝트 업무 승인 요청 알림 */
  TASK_APPROVAL_REQUEST("{username}님의 프로젝트 이력 등록 요청이 들어왔습니다."),

  /** 자격증 등록 승인 요청 알림 */
  CERTIFICATION_APPROVAL_REQUEST("{username}님의 자격증 등록 요청이 들어왔습니다.");

  private final String message;

  public String generateMessage(String username) {
    String safeUsername = username != null ? username : "";
    return this.message.replace("{username}", safeUsername);
  }

  public Notification toEntity(
      String senderId, String receiverId, String message, String contentId) {
    return Notification.builder()
        .senderId(senderId)
        .receiverId(receiverId)
        .message(message)
        .notificationType(this)
        .linkedContentId(contentId)
        .build();
  }
}
