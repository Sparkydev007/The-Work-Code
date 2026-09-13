package com.theworkcode.notification.repository;

import java.util.List;
import java.util.UUID;

import com.theworkcode.notification.entity.NotificationEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findTop50ByOrderByCreatedAtDesc();
}
