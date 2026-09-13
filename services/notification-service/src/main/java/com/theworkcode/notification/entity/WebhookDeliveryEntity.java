package com.theworkcode.notification.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "webhook_deliveries")
@Getter
@Setter
@NoArgsConstructor
public class WebhookDeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "webhook_id", nullable = false)
    private UUID webhookId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "attempt", nullable = false)
    private Integer attempt;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "delivered_at", nullable = false)
    private OffsetDateTime deliveredAt;
}
