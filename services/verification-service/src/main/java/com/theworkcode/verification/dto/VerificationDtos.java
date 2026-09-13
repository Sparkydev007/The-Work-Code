package com.theworkcode.verification.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public final class VerificationDtos {

    private VerificationDtos() {
    }

    public record CreateVerificationRequest(
            @NotBlank String verificationType,
            @NotBlank String purpose,
            @NotBlank String applicantName,
            String applicantEmail,
            String applicantDob,
            String employeeNumber,
            String employerName,
            Integer lookbackMonths,
            String idempotencyKey) {
    }

    public record CreateVerificationResponse(
            UUID id,
            String verificationCode,
            String status,
            String message) {
    }

    public record WorkflowStep(String step, String status, String detail, OffsetDateTime occurredAt) {
    }

    public record VerificationDetail(
            UUID id,
            String verificationCode,
            String verificationType,
            String purpose,
            String applicantName,
            String applicantEmail,
            String employeeNumber,
            String employerName,
            Integer lookbackMonths,
            String status,
            String result,
            Double confidence,
            Integer riskScore,
            String riskBand,
            String identityRef,
            String employmentRef,
            String incomeRef,
            String reportRef,
            String failureReason,
            OffsetDateTime createdAt,
            OffsetDateTime completedAt,
            List<WorkflowStep> workflow,
            Map<String, Object> subResults) {
    }

    public record VerificationSummary(
            UUID id,
            String verificationCode,
            String applicantName,
            String employerName,
            String verificationType,
            String status,
            String result,
            Double confidence,
            Integer riskScore,
            String riskBand,
            OffsetDateTime createdAt) {
    }

    public record VerificationPageResponse(List<VerificationSummary> content, int page, int size,
            long totalElements, int totalPages) {
    }

    // ---------------- Analytics ----------------

    public record DashboardMetrics(
            long totalRequests,
            long completed,
            long pending,
            long reviewRequired,
            long failed,
            double successRate,
            double avgProcessingSeconds) {
    }

    public record StatusCount(String status, long count) {
    }

    public record TypeCount(String type, long count) {
    }

    public record AnalyticsResponse(
            DashboardMetrics metrics,
            List<StatusCount> statusDistribution,
            List<TypeCount> typeDistribution) {
    }

    // ---------------- Disputes ----------------

    public record CreateDisputeRequest(
            UUID verificationId,
            String employeeNumber,
            @NotBlank String fieldName,
            @NotBlank String reportedValue,
            @NotBlank String claimedValue,
            String explanation) {
    }

    public record DisputeResponse(
            UUID id,
            String disputeCode,
            UUID verificationId,
            String employeeNumber,
            String fieldName,
            String reportedValue,
            String claimedValue,
            String explanation,
            String status,
            String resolution,
            String submittedBy,
            String reviewedBy,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
    }

    public record DisputePageResponse(List<DisputeResponse> content, int page, int size, long totalElements) {
    }

    public record DisputeStatusUpdate(String status, String resolution) {
    }

    // ---------------- Manual verification ----------------

    public record ManualQueueItem(
            UUID id,
            UUID verificationId,
            String employeeNumber,
            String employerName,
            String assignedAnalyst,
            String employerContact,
            String contactMethod,
            String notes,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime completedAt) {
    }

    public record ManualQueuePageResponse(List<ManualQueueItem> content, int page, int size, long totalElements) {
    }

    public record ManualAssignRequest(String assignedAnalyst, String employerContact, String contactMethod,
            String notes) {
    }

    // ---------------- Credentialing ----------------

    public record CredentialResponse(
            String organizationId,
            String organizationName,
            String verificationPurpose,
            String credentialStatus,
            boolean credentialed) {
    }
}
