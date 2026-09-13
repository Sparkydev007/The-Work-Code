package com.theworkcode.identity.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public final class IdentityDtos {

    private IdentityDtos() {
    }

    public record IdentityVerifyRequest(
            @NotBlank String name,
            String employeeNumber,
            String dateOfBirth,
            String email,
            String employer,
            String purpose) {
    }

    public record IdentityVerifyResponse(
            UUID verificationId,
            String result,
            double confidence,
            double nameScore,
            double employerScore,
            double employeeIdScore,
            double emailScore,
            String matchedEmployeeNumber,
            Map<String, Object> breakdown,
            OffsetDateTime createdAt) {
    }

    public record IdentityVerificationSummary(
            UUID id,
            String inputName,
            String inputEmployeeNumber,
            String result,
            double confidence,
            OffsetDateTime createdAt) {
    }

    public record IdentityPageResponse(java.util.List<IdentityVerificationSummary> content,
            int page, int size, long totalElements, int totalPages) {
    }
}
