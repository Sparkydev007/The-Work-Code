package com.theworkcode.notification.repository;

import java.util.List;
import java.util.UUID;

import com.theworkcode.notification.entity.WebhookDeliveryEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDeliveryEntity, UUID> {

    List<WebhookDeliveryEntity> findTop50ByWebhookIdOrderByDeliveredAtDesc(UUID webhookId);

    List<WebhookDeliveryEntity> findTop50ByOrderByDeliveredAtDesc();
}
