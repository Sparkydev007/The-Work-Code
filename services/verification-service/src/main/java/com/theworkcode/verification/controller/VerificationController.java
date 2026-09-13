package com.theworkcode.verification.controller;

import java.util.UUID;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.verification.dto.VerificationDtos.CreateVerificationRequest;
import com.theworkcode.verification.dto.VerificationDtos.CreateVerificationResponse;
import com.theworkcode.verification.dto.VerificationDtos.VerificationDetail;
import com.theworkcode.verification.dto.VerificationDtos.VerificationPageResponse;
import com.theworkcode.verification.service.VerificationOrchestrator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationOrchestrator orchestrator;

    @PostMapping
    public ApiResponse<CreateVerificationResponse> create(
            @Valid @RequestBody CreateVerificationRequest createRequest,
            @RequestParam(required = false) String simulate,
            HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        String user = RoleGuard.currentUser(request);
        String org = request.getHeader("X-Forwarded-User-Role") == null ? "ORG-8821" : "ORG-8821";
        return ApiResponse.ok(orchestrator.create(createRequest, user, org, simulate));
    }

    @GetMapping("/{idOrCode}")
    public ApiResponse<VerificationDetail> get(@PathVariable String idOrCode, HttpServletRequest request) {
        RoleGuard.require(request, "verification:read");
        return ApiResponse.ok(orchestrator.get(idOrCode));
    }

    @GetMapping
    public ApiResponse<VerificationPageResponse> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest request) {
        RoleGuard.require(request, "verification:read");
        return ApiResponse.ok(orchestrator.search(status, type, search, page, size));
    }

    @PostMapping("/{id}/rerun")
    public ApiResponse<CreateVerificationResponse> rerun(@PathVariable UUID id, HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        return ApiResponse.ok(orchestrator.rerun(id));
    }
}
