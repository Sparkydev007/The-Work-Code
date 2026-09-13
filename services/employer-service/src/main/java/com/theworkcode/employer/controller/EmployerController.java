package com.theworkcode.employer.controller;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.common.security.RoleGuard;
import com.theworkcode.employer.dto.EmployerDtos.CreateRequest;
import com.theworkcode.employer.dto.EmployerDtos.EmployerProfile;
import com.theworkcode.employer.dto.EmployerDtos.EmployerResponse;
import com.theworkcode.employer.dto.EmployerDtos.PageResponse;
import com.theworkcode.employer.service.EmployerService;

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
@RequestMapping("/api/v1/employers")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService employerService;

    @GetMapping
    public ApiResponse<PageResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String trustStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            HttpServletRequest request) {
        RoleGuard.require(request, "employers:read");
        return ApiResponse.ok(employerService.search(search, industry, trustStatus, page, size));
    }

    @GetMapping("/{employerCode}")
    public ApiResponse<EmployerResponse> get(@PathVariable String employerCode, HttpServletRequest request) {
        RoleGuard.require(request, "employers:read");
        return ApiResponse.ok(employerService.getByCode(employerCode));
    }

    @GetMapping("/{employerCode}/profile")
    public ApiResponse<EmployerProfile> profile(@PathVariable String employerCode, HttpServletRequest request) {
        RoleGuard.require(request, "employers:read");
        return ApiResponse.ok(employerService.getProfile(employerCode));
    }

    @PostMapping
    public ApiResponse<EmployerResponse> create(@Valid @RequestBody CreateRequest createRequest,
            HttpServletRequest request) {
        RoleGuard.require(request, "employers");
        return ApiResponse.ok(employerService.create(createRequest));
    }
}
