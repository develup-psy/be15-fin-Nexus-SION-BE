package com.nexus.sion.feature.notification.command.application.controller;

import java.nio.file.AccessDeniedException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.notification.command.application.dto.request.SquadShareNotificationRequest;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;

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
          String lastEventId)
      throws AccessDeniedException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new AccessDeniedException("인증되지 않음");
    }

    return ResponseEntity.ok(
        notificationCommandService.subscribe(userDetails.getUsername(), lastEventId));
  }

  @PatchMapping(value = "/reads")
  public ResponseEntity<ApiResponse<Void>> readAllNotification(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(
        ApiResponse.success(
            notificationCommandService.readAllNotification(userDetails.getUsername())));
  }

  @PatchMapping(value = "/reads/{id}")
  public ResponseEntity<ApiResponse<Void>> readAllNotification(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
    return ResponseEntity.ok(
        ApiResponse.success(
            notificationCommandService.readNotification(userDetails.getUsername(), id)));
  }

  @PostMapping(value = "squad-share")
  public ResponseEntity<ApiResponse<Void>> shareSquad(
      @RequestBody SquadShareNotificationRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    notificationCommandService.sendSquadShareNotification(
        userDetails.getUsername(), // senderId
        request.getReceiverId(),
        request.getSquadCode());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
