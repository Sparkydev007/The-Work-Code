package com.theworkcode.employee.controller;

import java.util.UUID;

import com.theworkcode.common.api.ApiResponse;
import com.theworkcode.employee.dto.EmployeeDtos.CreateRequest;
import com.theworkcode.employee.dto.EmployeeDtos.EmployeeResponse;
import com.theworkcode.employee.dto.EmployeeDtos.PageResponse;
import com.theworkcode.employee.service.EmployeeService;
import com.theworkcode.common.security.RoleGuard;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Employee API. All authorization is enforced server-side via RoleGuard.
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ApiResponse<PageResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employerCode,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "true") boolean masked,
            HttpServletRequest request) {
        RoleGuard.require(request, "employees:read");
        return ApiResponse.ok(employeeService.search(search, status, employerCode, department, page, size, masked));
    }

    @GetMapping("/{employeeNumber}")
    public ApiResponse<EmployeeResponse> get(@PathVariable String employeeNumber,
            @RequestParam(defaultValue = "false") boolean masked,
            HttpServletRequest request) {
        RoleGuard.require(request, "employees:read");
        return ApiResponse.ok(employeeService.getByEmployeeNumber(employeeNumber, masked));
    }

    @PostMapping
    public ApiResponse<EmployeeResponse> create(@Valid @RequestBody CreateRequest createRequest,
            HttpServletRequest request) {
        RoleGuard.require(request, "employees");
        return ApiResponse.ok(employeeService.create(createRequest));
    }

    @PutMapping("/{employeeNumber}")
    public ApiResponse<EmployeeResponse> update(@PathVariable String employeeNumber,
            @Valid @RequestBody CreateRequest updateRequest,
            HttpServletRequest request) {
        RoleGuard.require(request, "employees");
        return ApiResponse.ok(employeeService.update(employeeNumber, updateRequest));
    }

    @GetMapping("/internal/count")
    public ApiResponse<Long> count(@RequestParam(required = false) String employerCode) {
        // Internal metric endpoint used by analytics; unauthenticated read of a count only.
        return ApiResponse.ok(employerCode == null || employerCode.isBlank()
                ? employeeService.countAll()
                : employeeService.countByEmployerCode(employerCode));
    }
}
