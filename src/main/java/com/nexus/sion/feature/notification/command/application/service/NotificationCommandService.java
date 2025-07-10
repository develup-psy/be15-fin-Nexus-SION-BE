package com.nexus.sion.feature.notification.command.application.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;

public interface NotificationCommandService {
  void createAndSendNotification(
      String senderId, String receiverId, String message, NotificationType type, String linkedContentId);

  SseEmitter subscribe(String employeeIdentificationNumber, String lastEventId);

  Void readAllNotification(String employeeIdentificationNumber);

  Void readNotification(String employeeIdentificationNumber, Long notificationId);
}
