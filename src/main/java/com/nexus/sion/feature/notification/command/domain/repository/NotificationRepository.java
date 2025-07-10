package com.nexus.sion.feature.notification.command.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.feature.notification.command.domain.aggregate.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  @Modifying
  @Transactional
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiverId = :receiverId")
  void markAllAsRead(@Param("receiverId") String employeeIdentificationNumber);

  Optional<Notification> findByReceiverIdAndNotificationId(String receiverId, Long notificationId);
}
