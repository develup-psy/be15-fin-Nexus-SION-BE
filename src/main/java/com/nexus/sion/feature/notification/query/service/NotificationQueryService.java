package com.nexus.sion.feature.notification.query.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.generated.tables.pojos.Notification;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.notification.query.dto.NotificationDTO;
import com.nexus.sion.feature.notification.query.repository.NotificationQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {
  private final NotificationQueryRepository notificationQueryRepository;

  /* 알림 목록 조회 */
  @Transactional(readOnly = true)
  public PageResponse<NotificationDTO> getNotifications(String memberId, int page, int size) {

    List<Notification> notifications =
        notificationQueryRepository.selectNotifications(page, size, memberId);
    long totalElements = notificationQueryRepository.countTotalNotifications(memberId);

    // TODO : JOOQ pojo랑 notification dto랑 매칭

//    return PageResponse.fromJooq(response, totalElements, page, size);
    return null;
  }
}
