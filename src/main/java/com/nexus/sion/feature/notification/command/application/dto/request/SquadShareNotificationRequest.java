package com.nexus.sion.feature.notification.command.application.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SquadShareNotificationRequest {
  private String squadCode;
  private List<String> receivers;
}
