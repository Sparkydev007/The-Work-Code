package com.theworkcode.verification.controller;

import java.util.List;
import java.util.UUID;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.verification.dto.VerificationDtos.AnalyticsResponse;
import com.theworkcode.verification.dto.VerificationDtos.CreateDisputeRequest;
import com.theworkcode.verification.dto.VerificationDtos.CredentialResponse;
import com.theworkcode.verification.dto.VerificationDtos.DisputePageResponse;
import com.theworkcode.verification.dto.VerificationDtos.DisputeResponse;
import com.theworkcode.verification.dto.VerificationDtos.DisputeStatusUpdate;
import com.theworkcode.verification.dto.VerificationDtos.ManualAssignRequest;
import com.theworkcode.verification.dto.VerificationDtos.ManualQueueItem;
import com.theworkcode.verification.dto.VerificationDtos.ManualQueuePageResponse;
import com.theworkcode.verification.service.CredentialService;
import com.theworkcode.verification.service.DisputeService;
import com.theworkcode.verification.service.ManualVerificationService;
import com.theworkcode.verification.service.VerificationAnalyticsService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Disputes, manual verification queue, credentialing and analytics endpoints.
 */
@RestController
@RequiredArgsConstructor
public class SupportControllers {

    private final DisputeService disputeService;
    private final ManualVerificationService manualVerificationService;
    private final CredentialService credentialService;
    private final VerificationAnalyticsService analyticsService;

    // ---------------- Disputes ----------------

    @PostMapping("/api/v1/disputes")
    public ApiResponse<DisputeResponse> createDispute(@jakarta.validation.Valid @RequestBody CreateDisputeRequest disputeRequest,
            HttpServletRequest request) {
        String user = RoleGuard.currentUser(request);
        return ApiResponse.ok(disputeService.create(disputeRequest, user));
    }

    @GetMapping("/api/v1/disputes")
    public ApiResponse<DisputePageResponse> disputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest request) {
        RoleGuard.require(request, "disputes");
        return ApiResponse.ok(disputeService.list(page, size));
    }

    @GetMapping("/api/v1/disputes/{id}")
    public ApiResponse<DisputeResponse> dispute(@PathVariable UUID id, HttpServletRequest request) {
        RoleGuard.require(request, "disputes");
        return ApiResponse.ok(disputeService.get(id));
    }

    @PostMapping("/api/v1/disputes/{id}/status")
    public ApiResponse<DisputeResponse> updateDispute(@PathVariable UUID id,
            @RequestBody DisputeStatusUpdate statusUpdate, HttpServletRequest request) {
        RoleGuard.require(request, "disputes");
        String user = RoleGuard.currentUser(request);
        return ApiResponse.ok(disputeService.updateStatus(id, statusUpdate, user));
    }

    // ---------------- Manual verification queue ----------------

    @GetMapping("/api/v1/manual-verification")
    public ApiResponse<ManualQueuePageResponse> manualQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        return ApiResponse.ok(manualVerificationService.list(page, size));
    }

    @PostMapping("/api/v1/manual-verification/{id}/assign")
    public ApiResponse<ManualQueueItem> assign(@PathVariable UUID id,
            @RequestBody ManualAssignRequest assignRequest, HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        return ApiResponse.ok(manualVerificationService.assign(id, assignRequest));
    }

    @PostMapping("/api/v1/manual-verification/{id}/await-response")
    public ApiResponse<ManualQueueItem> awaitResponse(@PathVariable UUID id, HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        return ApiResponse.ok(manualVerificationService.awaitResponse(id));
    }

    @PostMapping("/api/v1/manual-verification/{id}/simulate-employer-response")
    public ApiResponse<ManualQueueItem> simulateEmployerResponse(@PathVariable UUID id, HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        return ApiResponse.ok(manualVerificationService.simulateEmployerResponse(id));
    }

    // ---------------- Credentialing ----------------

    @GetMapping("/api/v1/verifications/credentialing/{organizationId}")
    public ApiResponse<CredentialResponse> credential(@PathVariable String organizationId,
            HttpServletRequest request) {
        RoleGuard.require(request, "verification:read");
        return ApiResponse.ok(credentialService.getOrCreate(organizationId));
    }

    @PostMapping("/api/v1/verifications/credentialing/{organizationId}/activate-demo")
    public ApiResponse<CredentialResponse> activateDemo(@PathVariable String organizationId,
            HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        return ApiResponse.ok(credentialService.activateDemoCredentialing(organizationId));
    }

    // ---------------- Analytics ----------------

    @GetMapping("/api/v1/analytics/dashboard")
    public ApiResponse<AnalyticsResponse> dashboard(HttpServletRequest request) {
        RoleGuard.require(request, "analytics");
        return ApiResponse.ok(analyticsService.dashboard());
    }
}
