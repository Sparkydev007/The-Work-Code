package com.theworkcode.employment.controller;

import java.util.UUID;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.employment.dto.EmploymentDtos.HistoryResponse;
import com.theworkcode.employment.dto.EmploymentDtos.VerificationPageResponse;
import com.theworkcode.employment.dto.EmploymentDtos.VerificationResponse;
import com.theworkcode.employment.dto.EmploymentDtos.VerifyRequest;
import com.theworkcode.employment.service.EmploymentService;

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
@RequestMapping("/api/v1/employment")
@RequiredArgsConstructor
public class EmploymentController {

    private final EmploymentService employmentService;

    @PostMapping("/verifications")
    public ApiResponse<VerificationResponse> verify(@Valid @RequestBody VerifyRequest verifyRequest,
            HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        String requestId = com.theworkcode.common.correlation.RequestContext.getRequestId();
        return ApiResponse.ok(employmentService.verify(verifyRequest, requestId));
    }

    @GetMapping("/verifications/{id}")
    public ApiResponse<VerificationResponse> get(@PathVariable UUID id, HttpServletRequest request) {
        RoleGuard.require(request, "verification:read");
        return ApiResponse.ok(employmentService.get(id));
    }

    @GetMapping("/verifications")
    public ApiResponse<VerificationPageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest request) {
        RoleGuard.require(request, "verification:read");
        return ApiResponse.ok(employmentService.history(page, size));
    }

    @GetMapping("/employees/{employeeNumber}/history")
    public ApiResponse<HistoryResponse> history(@PathVariable String employeeNumber, HttpServletRequest request) {
        RoleGuard.require(request, "employees:read");
        return ApiResponse.ok(employmentService.history(employeeNumber));
    }
}
