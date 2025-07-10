package com.nexus.sion.feature.notification.command.domain.aggregate;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_id")
  private Long notificationId;

  @Column(name = "receiver_id", nullable = false)
  private String receiverId;

  @Column(name = "sender_id")
  private String senderId;

  @Column(name = "is_read")
  private boolean isRead = false;

  @Column(name = "linked_content_id")
  private String linkedContentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type", nullable = false)
  private NotificationType notificationType;

  @Column(name = "message")
  private String message;

  /* 원하는 값만 넣을 수 있는 빌더 메소드 */
  @Builder
  public Notification(
      String senderId,
      String receiverId,
      String linkedContentId,
      NotificationType notificationType,
      String message,
      boolean isRead) {
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.linkedContentId = linkedContentId;
    this.notificationType = notificationType;
    this.message = message;
    this.isRead = isRead;
  }

  public void setHasRead() {
    isRead = true;
  }
}
