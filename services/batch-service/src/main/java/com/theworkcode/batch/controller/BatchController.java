package com.theworkcode.batch.controller;

import com.theworkcode.batch.entity.BatchItemEntity;
import com.theworkcode.batch.entity.BatchJobEntity;
import com.theworkcode.batch.service.BatchService;
import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping("/api/v1/batches")
    public ApiResponse<BatchService.UploadResult> upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        RoleGuard.require(request, "batch");
        return ApiResponse.ok(batchService.upload(file, RoleGuard.currentUser(request)));
    }

    @PostMapping("/api/v1/batches/{batchCode}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable String batchCode, HttpServletRequest request) {
        RoleGuard.require(request, "batch");
        return ApiResponse.ok(batchService.submit(batchCode));
    }

    @GetMapping("/api/v1/batches/{batchCode}")
    public ApiResponse<BatchJobEntity> get(@PathVariable String batchCode, HttpServletRequest request) {
        RoleGuard.require(request, "batch");
        return ApiResponse.ok(batchService.get(batchCode));
    }

    @GetMapping("/api/v1/batches/{batchCode}/results")
    public ApiResponse<List<BatchItemEntity>> results(@PathVariable String batchCode,
            HttpServletRequest request) {
        RoleGuard.require(request, "batch");
        return ApiResponse.ok(batchService.items(batchCode));
    }

    @GetMapping("/api/v1/batches/{batchCode}/download")
    public ResponseEntity<byte[]> download(@PathVariable String batchCode, HttpServletRequest request) {
        RoleGuard.require(request, "batch");
        byte[] csv = batchService.resultCsv(batchCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", batchCode + "-results.csv");
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
}
