package com.nexus.sion.feature.notification.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SquadShareNotificationRequest {
  private String squadCode;
  private List<String> receivers;
}
