package com.nexus.sion.feature.notification.command.application.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "알림 API", description = "알림 연결, 발송 API")
public class NotificationCommandController {
  private final NotificationCommandService notificationCommandService;

  @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> subscribe(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(value = "Last-Event-Id", required = false, defaultValue = "")
          String lastEventId) {
    return ResponseEntity.ok(
        notificationCommandService.subscribe(userDetails.getUsername(), lastEventId));
  }

  /* 테스트용 메소드, 알림 전송 로직 추가할 때 아래 보고 추가하심 됩니다 */
  @GetMapping("/send")
  public ResponseEntity<String> sendTestNotification(
      @AuthenticationPrincipal UserDetails userDetails) {

    notificationCommandService.createAndSendNotification(
        userDetails.getUsername(),
        "0120250001",
        "자격증 승인 부탁드립니다.",
        NotificationType.CERTIFICATION_APPROVAL_REQUEST,
        "123L");
    return ResponseEntity.ok("알림 전송 완료");
  }
}
