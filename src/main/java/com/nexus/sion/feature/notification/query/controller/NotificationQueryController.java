package com.nexus.sion.feature.notification.query.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.notification.query.dto.NotificationDTO;
import com.nexus.sion.feature.notification.query.service.NotificationQueryService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "알림 API", description = "알림 조회")
public class NotificationQueryController {
  private final NotificationQueryService notificationQueryService;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<PageResponse<NotificationDTO>>> getNotificationList(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(defaultValue = "0") int page, // 기본값 0
      @RequestParam(defaultValue = "10") int size // 기본값 10
      ) {
    PageResponse<NotificationDTO> notifications =
        notificationQueryService.getNotifications(userDetails.getUsername(), page, size);
    return ResponseEntity.ok(ApiResponse.success(notifications));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<NotificationDTO>>> getAllNotificationList(
      @RequestParam(defaultValue = "0") int page, // 기본값 0
      @RequestParam(defaultValue = "10") int size // 기본값 10
      ) {
    PageResponse<NotificationDTO> notifications =
        notificationQueryService.getAllNotifications(page, size);
    return ResponseEntity.ok(ApiResponse.success(notifications));
  }
}
