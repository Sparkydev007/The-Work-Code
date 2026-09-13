package com.theworkcode.employer.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

import com.theworkcode.employer.entity.EmployerEntity;

public final class EmployerDtos {

    private EmployerDtos() {
    }

    public record CreateRequest(
            @NotBlank String name,
            String industry,
            String city,
            Integer employeeCount,
            String trustStatus) {
    }

    public record EmployerResponse(
            UUID id,
            String employerCode,
            String name,
            String industry,
            String city,
            Integer employeeCount,
            String trustStatus,
            LocalDate contributorSince,
            LocalDate lastDataUpdate,
            String dataSource,
            String refreshFrequency,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

        public static EmployerResponse from(EmployerEntity e) {
            return new EmployerResponse(e.getId(), e.getEmployerCode(), e.getName(), e.getIndustry(),
                    e.getCity(), e.getEmployeeCount(), e.getTrustStatus(), e.getContributorSince(),
                    e.getLastDataUpdate(), e.getDataSource(), e.getRefreshFrequency(),
                    e.getCreatedAt(), e.getUpdatedAt());
        }
    }

    public record PageResponse(List<EmployerResponse> content, int page, int size,
            long totalElements, int totalPages) {
    }

    /** Employer profile enriched with cross-service employee statistics. */
    public record EmployerProfile(
            EmployerResponse employer,
            long activeEmployeeRecords,
            String employeeServiceStatus) {
    }
}
