package com.theworkcode.notification.repository;

import java.util.UUID;

import com.theworkcode.notification.entity.WebhookEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<WebhookEntity, UUID> {
}
