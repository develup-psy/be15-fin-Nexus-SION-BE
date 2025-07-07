package com.nexus.sion.feature.notification.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.notification.command.domain.aggregate.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {}
