package com.theworkcode.notification.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.notification.entity.NotificationEntity;
import com.theworkcode.notification.entity.WebhookDeliveryEntity;
import com.theworkcode.notification.entity.WebhookEntity;
import com.theworkcode.notification.repository.NotificationRepository;
import com.theworkcode.notification.repository.WebhookDeliveryRepository;
import com.theworkcode.notification.repository.WebhookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demo webhook engine. Events:
 * verification.created, verification.processing, verification.completed,
 * verification.review_required, verification.failed, report.generated,
 * dispute.created.
 *
 * Delivery is simulated (no external HTTP): outcomes are deterministic per
 * delivery ID, with retry support and full delivery logs. A production
 * deployment would replace the simulated dispatcher with an HTTP client
 * behind the same interface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    public static final List<String> SUPPORTED_EVENTS = List.of(
            "verification.created",
            "verification.processing",
            "verification.completed",
            "verification.review_required",
            "verification.failed",
            "report.generated",
            "dispute.created");

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public WebhookEntity create(String url, String description, List<String> events) {
        for (String event : events) {
            if (!SUPPORTED_EVENTS.contains(event)) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "Unsupported event type: " + event);
            }
        }
        WebhookEntity e = new WebhookEntity();
        e.setUrl(url);
        e.setDescription(description);
        e.setEvents(String.join(",", events));
        e.setStatus("ACTIVE");
        e.setSecretHint("whsec_" + UUID.randomUUID().toString().substring(0, 8) + "•••");
        e = webhookRepository.save(e);
        log.info("webhook_created url={} events={}", url, events.size());
        return e;
    }

    @Transactional(readOnly = true)
    public List<WebhookEntity> list() {
        return webhookRepository.findAll();
    }

    @Transactional
    public WebhookEntity revoke(UUID id) {
        WebhookEntity e = find(id);
        e.setStatus("REVOKED");
        return webhookRepository.save(e);
    }

    /** Simulated test delivery: deterministic success/failure + logged attempt. */
    @Transactional
    public WebhookDeliveryEntity test(UUID id, String eventType, boolean simulateFailure) {
        WebhookEntity webhook = find(id);
        if (!"ACTIVE".equals(webhook.getStatus())) {
            throw new ApiException(ErrorCode.CONFLICT, "Webhook is not active.");
        }
        String resolvedEvent = eventType == null || eventType.isBlank() ? "verification.completed" : eventType;
        boolean success = !simulateFailure && Math.abs(id.hashCode() + resolvedEvent.hashCode()) % 10 != 2;

        WebhookDeliveryEntity delivery = new WebhookDeliveryEntity();
        delivery.setWebhookId(id);
        delivery.setEventType(resolvedEvent);
        delivery.setPayload(writeJson(Map.of(
                "event", delivery.getEventType(),
                "webhookId", String.valueOf(id),
                "timestamp", OffsetDateTime.now().toString(),
                "demo", true)));
        delivery.setStatus(success ? "SUCCESS" : "FAILED");
        delivery.setAttempt(1);
        delivery.setResponseCode(success ? 200 : 503);
        delivery.setDurationMs(80 + Math.abs(id.hashCode()) % 300);
        delivery.setDeliveredAt(OffsetDateTime.now());
        delivery = deliveryRepository.save(delivery);

        // Also surface as an in-app notification
        NotificationEntity n = new NotificationEntity();
        n.setEventType(delivery.getEventType());
        n.setTitle(success ? "Webhook delivered: " + delivery.getEventType()
                : "Webhook failed: " + delivery.getEventType());
        n.setBody(success ? "Delivered to " + webhook.getUrl()
                : "Delivery to " + webhook.getUrl() + " failed with 503. Retry available.");
        n.setSeverity(success ? "INFO" : "ERROR");
        notificationRepository.save(n);

        log.info("webhook_tested id={} event={} status={}", id, delivery.getEventType(), delivery.getStatus());
        return delivery;
    }

    @Transactional
    public WebhookDeliveryEntity retry(UUID deliveryId) {
        WebhookDeliveryEntity failed = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Delivery not found."));
        WebhookDeliveryEntity retry = new WebhookDeliveryEntity();
        retry.setWebhookId(failed.getWebhookId());
        retry.setEventType(failed.getEventType());
        retry.setPayload(failed.getPayload());
        boolean success = Math.abs(deliveryId.hashCode()) % 4 != 0; // retries mostly succeed
        retry.setStatus(success ? "SUCCESS" : "FAILED");
        retry.setAttempt(failed.getAttempt() + 1);
        retry.setResponseCode(success ? 200 : 503);
        retry.setDurationMs(90 + Math.abs(deliveryId.hashCode()) % 200);
        retry.setDeliveredAt(OffsetDateTime.now());
        return deliveryRepository.save(retry);
    }

    @Transactional(readOnly = true)
    public List<WebhookDeliveryEntity> deliveries(UUID webhookId) {
        return webhookId == null
                ? deliveryRepository.findTop50ByOrderByDeliveredAtDesc()
                : deliveryRepository.findTop50ByWebhookIdOrderByDeliveredAtDesc(webhookId);
    }

    @Transactional(readOnly = true)
    public List<NotificationEntity> notifications() {
        return notificationRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void seedDemoIfEmpty() {
        if (webhookRepository.count() > 0) {
            return;
        }
        WebhookEntity w1 = create("https://hooks.meridianlender-demo.com/workcode", 
                "Meridian Lending Group - verification outcomes", 
                List.of("verification.completed", "verification.review_required", "verification.failed"));
        create("https://hooks.hrisuite-demo.com/audit",
                "HRiSuite - dispute and report events",
                List.of("dispute.created", "report.generated"));

        // Seed delivery history: mostly success, two failures for demo
        for (int i = 0; i < 12; i++) {
            WebhookDeliveryEntity d = new WebhookDeliveryEntity();
            d.setWebhookId(w1.getId());
            d.setEventType(SUPPORTED_EVENTS.get(i % SUPPORTED_EVENTS.size()));
            d.setPayload(writeJson(Map.of("event", d.getEventType(), "seq", i)));
            boolean failed = i == 4 || i == 9;
            d.setStatus(failed ? "FAILED" : "SUCCESS");
            d.setAttempt(1);
            d.setResponseCode(failed ? 503 : 200);
            d.setDurationMs(80 + i * 13);
            d.setDeliveredAt(OffsetDateTime.now().minusHours(i * 3L));
            deliveryRepository.save(d);
        }
        log.info("webhook_seed_complete");
    }

    private WebhookEntity find(UUID id) {
        return webhookRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.API_KEY_NOT_FOUND, "Webhook not found."));
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
