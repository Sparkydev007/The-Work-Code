package com.theworkcode.notification.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.notification.entity.NotificationEntity;
import com.theworkcode.notification.entity.WebhookDeliveryEntity;
import com.theworkcode.notification.entity.WebhookEntity;
import com.theworkcode.notification.service.WebhookService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    public record CreateWebhookRequest(String url, String description, List<String> events) {
    }

    @PostMapping
    public ApiResponse<WebhookEntity> create(@RequestBody CreateWebhookRequest body, HttpServletRequest request) {
        RoleGuard.require(request, "webhooks");
        return ApiResponse.ok(webhookService.create(body.url(), body.description(), body.events()));
    }

    @GetMapping
    public ApiResponse<List<WebhookEntity>> list(HttpServletRequest request) {
        RoleGuard.require(request, "webhooks");
        return ApiResponse.ok(webhookService.list());
    }

    @PostMapping("/{id}/revoke")
    public ApiResponse<WebhookEntity> revoke(@PathVariable UUID id, HttpServletRequest request) {
        RoleGuard.require(request, "webhooks");
        return ApiResponse.ok(webhookService.revoke(id));
    }

    @PostMapping("/{id}/test")
    public ApiResponse<WebhookDeliveryEntity> test(@PathVariable UUID id,
            @RequestParam(required = false) String event,
            @RequestParam(defaultValue = "false") boolean simulateFailure,
            HttpServletRequest request) {
        RoleGuard.require(request, "webhooks");
        return ApiResponse.ok(webhookService.test(id, event, simulateFailure));
    }

    @PostMapping("/deliveries/{deliveryId}/retry")
    public ApiResponse<WebhookDeliveryEntity> retry(@PathVariable UUID deliveryId, HttpServletRequest request) {
        RoleGuard.require(request, "webhooks");
        return ApiResponse.ok(webhookService.retry(deliveryId));
    }

    @GetMapping("/deliveries")
    public ApiResponse<List<WebhookDeliveryEntity>> deliveries(
            @RequestParam(required = false) UUID webhookId,
            HttpServletRequest request) {
        RoleGuard.require(request, "webhooks");
        return ApiResponse.ok(webhookService.deliveries(webhookId));
    }

    @GetMapping("/notifications")
    public ApiResponse<List<NotificationEntity>> notifications(HttpServletRequest request) {
        RoleGuard.require(request, "webhooks");
        return ApiResponse.ok(webhookService.notifications());
    }

    @GetMapping("/events")
    public ApiResponse<Map<String, Object>> events(HttpServletRequest request) {
        RoleGuard.require(request, "webhooks");
        return ApiResponse.ok(Map.of("events", WebhookService.SUPPORTED_EVENTS));
    }
}
