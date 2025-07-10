package com.nexus.sion.feature.notification.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SquadShareNotificationRequest {
  private String squadCode;
  private String receiverId;
}
