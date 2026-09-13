package com.theworkcode.audit.controller;

import java.util.Map;

import com.theworkcode.audit.entity.AuditEventEntity;
import com.theworkcode.audit.service.AuditService;
import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    public record AuditEntryRequest(
            String action,
            String resourceType,
            String resourceId,
            String result,
            String detail) {
    }

    /** Append an audit event. Called by other services (server-to-server) and the gateway. */
    @PostMapping
    public ApiResponse<AuditEventEntity> record(@RequestBody AuditEntryRequest body, HttpServletRequest request) {
        // Any authenticated principal may append; audit writes are part of the platform contract.
        return ApiResponse.ok(auditService.record(
                RoleGuard.currentUser(request),
                request.getHeader(com.theworkcode.common.security.RoleGuard.HEADER_ROLE),
                body.action(),
                body.resourceType(),
                body.resourceId(),
                com.theworkcode.common.correlation.RequestContext.getRequestId(),
                body.result(),
                body.detail(),
                "ORG-8821"));
    }

    @GetMapping
    public ApiResponse<Page<AuditEventEntity>> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String resourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest request) {
        RoleGuard.require(request, "audit:read");
        return ApiResponse.ok(auditService.search(action, actor, resourceId, page, size));
    }
}
