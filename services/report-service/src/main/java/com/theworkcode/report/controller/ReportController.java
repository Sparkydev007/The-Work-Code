package com.theworkcode.report.controller;

import java.util.Map;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.report.entity.ReportEntity;
import com.theworkcode.report.service.ReportService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ApiResponse<Map<String, Object>> generate(@RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        RoleGuard.require(request, "reports");
        String user = RoleGuard.currentUser(request);
        return ApiResponse.ok(reportService.generate(body, user));
    }

    @GetMapping
    public ApiResponse<Page<ReportEntity>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest request) {
        RoleGuard.require(request, "reports:read");
        Page<ReportEntity> reports = reportService.list(page, size);
        return ApiResponse.ok(reports);
    }

    @GetMapping("/{reportCode}")
    public ApiResponse<ReportEntity> get(@PathVariable String reportCode, HttpServletRequest request) {
        RoleGuard.require(request, "reports:read");
        return ApiResponse.ok(reportService.get(reportCode));
    }

    @GetMapping("/{reportCode}/download")
    public ResponseEntity<byte[]> download(@PathVariable String reportCode, HttpServletRequest request) {
        RoleGuard.require(request, "reports:read");
        byte[] pdf = reportService.download(reportCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", reportCode + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PostMapping("/{reportCode}/regenerate")
    public ApiResponse<Map<String, Object>> regenerate(@PathVariable String reportCode,
            HttpServletRequest request) {
        RoleGuard.require(request, "reports");
        return ApiResponse.ok(reportService.regenerate(reportCode, RoleGuard.currentUser(request)));
    }
}
