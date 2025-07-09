package com.nexus.sion.feature.notification.command.application.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.AccessDeniedException;

import org.springframework.security.core.Authentication;

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
          String lastEventId) throws AccessDeniedException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new AccessDeniedException("인증되지 않음");
    }

    return ResponseEntity.ok(
        notificationCommandService.subscribe(userDetails.getUsername(), lastEventId));
  }
}
