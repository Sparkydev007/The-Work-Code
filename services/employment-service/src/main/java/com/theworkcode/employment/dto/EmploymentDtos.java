package com.theworkcode.employment.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public final class EmploymentDtos {

    private EmploymentDtos() {
    }

    public record VerifyRequest(
            @NotBlank String employeeNumber,
            String employerName,
            Integer lookbackMonths) {
    }

    public record EmploymentRecordResponse(
            UUID id,
            String employeeNumber,
            String employerName,
            String employerCode,
            String jobTitle,
            String department,
            String status,
            String employmentType,
            LocalDate hireDate,
            LocalDate terminationDate,
            String workLocation,
            String source,
            double confidence,
            OffsetDateTime createdAt) {

        public static EmploymentRecordResponse from(com.theworkcode.employment.entity.EmploymentRecordEntity e) {
            return new EmploymentRecordResponse(e.getId(), e.getEmployeeNumber(), e.getEmployerName(),
                    e.getEmployerCode(), e.getJobTitle(), e.getDepartment(), e.getStatus(),
                    e.getEmploymentType(), e.getHireDate(), e.getTerminationDate(), e.getWorkLocation(),
                    e.getSource(), e.getConfidence().doubleValue(), e.getCreatedAt());
        }
    }

    public record VerificationResponse(
            UUID id,
            String employeeNumber,
            String employerName,
            String employmentStatus,
            String jobTitle,
            String department,
            LocalDate hireDate,
            LocalDate terminationDate,
            String result,
            double confidence,
            Integer lookbackMonths,
            Map<String, Object> details,
            OffsetDateTime createdAt) {
    }

    public record HistoryResponse(
            String employeeNumber,
            List<TenureResponse> tenures,
            boolean activeEmployment) {
    }

    public record TenureResponse(
            String employerName,
            String jobTitle,
            String department,
            String status,
            String employmentType,
            LocalDate startDate,
            LocalDate endDate,
            String workLocation,
            String source,
            double confidence) {
    }

    public record VerificationPageResponse(List<VerificationResponse> content, int page, int size,
            long totalElements, int totalPages) {
    }
}
