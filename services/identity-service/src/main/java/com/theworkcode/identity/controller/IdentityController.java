package com.theworkcode.identity.controller;

import java.util.UUID;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.identity.dto.IdentityDtos.IdentityPageResponse;
import com.theworkcode.identity.dto.IdentityDtos.IdentityVerifyRequest;
import com.theworkcode.identity.dto.IdentityDtos.IdentityVerifyResponse;
import com.theworkcode.identity.service.IdentityVerificationService;

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
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
public class IdentityController {

    private final IdentityVerificationService identityVerificationService;

    @PostMapping("/verify")
    public ApiResponse<IdentityVerifyResponse> verify(@Valid @RequestBody IdentityVerifyRequest verifyRequest,
            HttpServletRequest request) {
        RoleGuard.require(request, "identity");
        String requestId = com.theworkcode.common.correlation.RequestContext.getRequestId();
        return ApiResponse.ok(identityVerificationService.verify(verifyRequest, requestId));
    }

    @GetMapping("/verifications/{id}")
    public ApiResponse<IdentityVerifyResponse> get(@PathVariable UUID id, HttpServletRequest httpRequest) {
        RoleGuard.require(httpRequest, "identity");
        return ApiResponse.ok(identityVerificationService.get(id));
    }

    @GetMapping("/verifications")
    public ApiResponse<IdentityPageResponse> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest httpRequest) {
        RoleGuard.require(httpRequest, "identity");
        return ApiResponse.ok(identityVerificationService.history(page, size));
    }
}
