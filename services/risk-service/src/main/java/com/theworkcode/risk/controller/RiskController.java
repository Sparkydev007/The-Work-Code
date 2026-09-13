package com.theworkcode.risk.controller;

import java.util.Map;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.risk.engine.RiskEngine.Assessment;
import com.theworkcode.risk.entity.RiskAnomalyEntity;
import com.theworkcode.risk.service.RiskService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @PostMapping("/analyze")
    public ApiResponse<Assessment> analyze(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        RoleGuard.require(request, "verification");
        return ApiResponse.ok(riskService.analyze(body));
    }

    @GetMapping("/{verificationId}")
    public ApiResponse<Assessment> byVerification(@PathVariable String verificationId, HttpServletRequest request) {
        RoleGuard.require(request, "verification:read");
        return ApiResponse.ok(riskService.getByVerificationId(verificationId));
    }

    @GetMapping("/anomalies")
    public ApiResponse<Page<RiskAnomalyEntity>> anomalies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest request) {
        RoleGuard.require(request, "risk");
        return ApiResponse.ok(riskService.anomalies(page, size));
    }

    @GetMapping("/anomalies/summary")
    public ApiResponse<Map<String, Long>> summary(HttpServletRequest request) {
        RoleGuard.require(request, "risk");
        return ApiResponse.ok(Map.of(
                "open", riskService.openAnomalies(),
                "critical", riskService.criticalAnomalies()));
    }
}
