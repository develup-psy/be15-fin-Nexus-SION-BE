package com.nexus.sion.feature.notification.query.repository;

import static com.example.jooq.generated.Tables.MEMBER;
import static com.example.jooq.generated.tables.Notification.NOTIFICATION;

import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.notification.query.dto.NotificationDTO;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {
  private final DSLContext dsl;

  public List<NotificationDTO> selectNotifications(
      int page, int size, String employeeIdentificationNumber) {
    int offset = Math.max(page, 0) * size;

    return dsl.select(
            NOTIFICATION.NOTIFICATION_ID,
            NOTIFICATION.NOTIFICATION_TYPE,
            NOTIFICATION.LINKED_CONTENT_ID,
            NOTIFICATION.MESSAGE,
            NOTIFICATION.IS_READ,
            NOTIFICATION.CREATED_AT,
            NOTIFICATION.SENDER_ID,
            MEMBER.EMPLOYEE_NAME.as("senderName"),
            NOTIFICATION.RECEIVER_ID)
        .from(NOTIFICATION)
        .leftJoin(MEMBER)
        .on(NOTIFICATION.SENDER_ID.eq(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
        .where(NOTIFICATION.RECEIVER_ID.eq(employeeIdentificationNumber))
        .orderBy(NOTIFICATION.CREATED_AT.desc())
        .limit(size)
        .offset(offset)
        .fetchInto(NotificationDTO.class);
  }

  public long countTotalNotifications(String employeeIdentificationNumber) {
    return Optional.ofNullable(
            dsl.selectCount()
                .from(NOTIFICATION)
                .where(NOTIFICATION.RECEIVER_ID.eq(employeeIdentificationNumber))
                .fetchOne(0, Long.class))
        .orElse(0L);
  }

  public List<NotificationDTO> selectAllNotifications(int page, int size) {
    int offset = Math.max(page, 0) * size;

    return dsl.select(
            NOTIFICATION.NOTIFICATION_ID,
            NOTIFICATION.NOTIFICATION_TYPE,
            NOTIFICATION.LINKED_CONTENT_ID,
            NOTIFICATION.MESSAGE,
            NOTIFICATION.IS_READ,
            NOTIFICATION.CREATED_AT,
            NOTIFICATION.SENDER_ID,
            MEMBER.EMPLOYEE_NAME.as("senderName"),
            NOTIFICATION.RECEIVER_ID)
        .from(NOTIFICATION)
        .leftJoin(MEMBER)
        .on(NOTIFICATION.SENDER_ID.eq(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
        .orderBy(NOTIFICATION.CREATED_AT.desc())
        .limit(size)
        .offset(offset)
        .fetchInto(NotificationDTO.class);
  }

  public long countTotalAllNotifications() {
    return Optional.ofNullable(dsl.selectCount().from(NOTIFICATION).fetchOne(0, Long.class))
        .orElse(0L);
  }
}
