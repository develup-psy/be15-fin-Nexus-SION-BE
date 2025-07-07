package com.nexus.sion.feature.notification.domain.aggregate;

public enum NotificationType {

    /** 업무 업로드 요청 알림 */
    TASK_UPLOAD_REQUEST,

    /** FP 분석 완료 알림 */
    FP_ANALYSIS_COMPLETE,

    /** 스쿼드 댓글 알림 */
    SQUAD_COMMENT,

    /** 스쿼드 공유 알림 */
    SQUAD_SHARE,

    /** 등급 변경 알림 */
    GRADE_CHANGE,

    /** 프로젝트 업무 승인 요청 알림 */
    TASK_APPROVAL_REQUEST,

    /** 자격증 등록 승인 요청 알림 */
    CERTIFICATION_APPROVAL_REQUEST
}
