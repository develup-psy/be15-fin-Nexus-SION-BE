package com.nexus.sion.feature.notification.command.application.service;

import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationCommandService {
    void createAndSendNotification(String senderId, String receiverId, String message, NotificationType type, String linkedContentId);

    SseEmitter subscribe(String employeeIdentificationNumber, String lastEventId);
}
