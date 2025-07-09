package com.nexus.sion.feature.notification.query.dto;

import java.time.LocalDateTime;

import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationDTO {
  Long notificationId;
  NotificationType notificationType;
  String linkedContentId;
  String message;
  Boolean isRead;
  LocalDateTime createdAt;
  String senderId;
  String senderName;
  String receiverId;
}
