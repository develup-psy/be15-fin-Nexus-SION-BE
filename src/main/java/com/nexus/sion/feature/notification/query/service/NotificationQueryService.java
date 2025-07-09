package com.nexus.sion.feature.notification.query.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.notification.query.dto.NotificationDTO;
import com.nexus.sion.feature.notification.query.repository.NotificationQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationQueryService {
  private final NotificationQueryRepository notificationQueryRepository;
  private final MemberRepository memberRepository;

  /* 알림 목록 조회 */
  @Transactional(readOnly = true)
  public PageResponse<NotificationDTO> getNotifications(String memberId, int page, int size) {

    log.info("memberId" + memberId);
    log.info("page" + page);
    log.info("size" + size);

    List<NotificationDTO> notifications =
        notificationQueryRepository.selectNotifications(page, size, memberId).stream()
            .map(
                notification -> {
                  String senderName =
                      memberRepository
                          .findById(notification.getSenderId())
                          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                          .getEmployeeName();
                  return NotificationDTO.builder()
                      .notificationId(notification.getNotificationId())
                      .notificationType(
                          NotificationType.valueOf(notification.getNotificationType()))
                      .linkedContentId(notification.getLinkedContentId())
                      .message(notification.getMessage())
                      .isRead(notification.getIsRead() == 1)
                      .createdAt(notification.getCreatedAt())
                      .receiverId(notification.getReceiverId())
                      .senderId(notification.getSenderId())
                      .senderName(senderName)
                      .build();
                })
            .toList();

    long totalElements = notificationQueryRepository.countTotalNotifications(memberId);

    return PageResponse.fromJooq(notifications, totalElements, page, size);
  }
}
