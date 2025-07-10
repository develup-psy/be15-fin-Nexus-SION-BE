package com.nexus.sion.feature.notification.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.notification.command.domain.aggregate.Notification;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = 1 WHERE n.receiverId = :receiverId")
    void markAllAsRead(@Param("receiverId") String employeeIdentificationNumber);
}
