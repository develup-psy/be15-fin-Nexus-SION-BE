package com.nexus.sion.feature.notification.command.application.dto.response;

import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSendResponse {

    private Long notificationId;
    private NotificationType notificationType;
    private String linkedContentId;
    private String message;
    private boolean isRead;
    private String receiverId;
}
