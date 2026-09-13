package com.theworkcode.income.controller;

import java.util.UUID;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.income.dto.IncomeDtos.IncomeSummaryResponse;
import com.theworkcode.income.dto.IncomeDtos.IncomeTrendResponse;
import com.theworkcode.income.dto.IncomeDtos.PayPeriodPageResponse;
import com.theworkcode.income.dto.IncomeDtos.VerificationPageResponse;
import com.theworkcode.income.dto.IncomeDtos.VerificationResponse;
import com.theworkcode.income.dto.IncomeDtos.VerifyRequest;
import com.theworkcode.income.service.IncomeService;

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
@RequestMapping("/api/v1/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping("/verifications")
    public ApiResponse<VerificationResponse> verify(@Valid @RequestBody VerifyRequest verifyRequest,
            HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        String requestId = com.theworkcode.common.correlation.RequestContext.getRequestId();
        return ApiResponse.ok(incomeService.verify(verifyRequest, requestId));
    }

    @GetMapping("/verifications/{id}")
    public ApiResponse<VerificationResponse> get(@PathVariable UUID id, HttpServletRequest httpRequest) {
        RoleGuard.require(httpRequest, "verification:read");
        return ApiResponse.ok(incomeService.get(id));
    }

    @GetMapping("/verifications")
    public ApiResponse<VerificationPageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest httpRequest) {
        RoleGuard.require(httpRequest, "verification:read");
        return ApiResponse.ok(incomeService.list(page, size));
    }

    @GetMapping("/employees/{employeeNumber}/summary")
    public ApiResponse<IncomeSummaryResponse> summary(@PathVariable String employeeNumber,
            HttpServletRequest httpRequest) {
        RoleGuard.require(httpRequest, "income:read");
        return ApiResponse.ok(incomeService.summary(employeeNumber));
    }

    @GetMapping("/employees/{employeeNumber}/pay-periods")
    public ApiResponse<PayPeriodPageResponse> payPeriods(@PathVariable String employeeNumber,
            @RequestParam(required = false) Integer lookbackMonths,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest httpRequest) {
        RoleGuard.require(httpRequest, "income:read");
        return ApiResponse.ok(incomeService.payPeriods(employeeNumber, lookbackMonths, page, size));
    }

    @GetMapping("/employees/{employeeNumber}/trend")
    public ApiResponse<IncomeTrendResponse> trend(@PathVariable String employeeNumber,
            @RequestParam(required = false) Integer lookbackMonths,
            HttpServletRequest httpRequest) {
        RoleGuard.require(httpRequest, "income:read");
        return ApiResponse.ok(incomeService.trend(employeeNumber, lookbackMonths));
    }
}
