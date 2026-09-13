package com.theworkcode.common.api;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard API error envelope.
 *
 * <pre>
 * {
 *   "success": false,
 *   "error": { "code": "EMPLOYEE_NOT_FOUND", "message": "..." },
 *   "requestId": "REQ-12345"
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorBody(boolean success, ErrorBody error, String requestId, Instant timestamp) {

    public record ErrorBody(String code, String message, Map<String, Object> details, String recommendedAction) {
    }

    public static ApiErrorBody of(ErrorCode code, String message, Map<String, Object> details) {
        return new ApiErrorBody(
                false,
                new ErrorBody(code.name(), message, details, recommendedActionFor(code)),
                com.theworkcode.common.correlation.RequestContext.getRequestId(),
                Instant.now());
    }

    private static String recommendedActionFor(ErrorCode code) {
        return switch (code) {
            case UNAUTHORIZED -> "Sign in or supply a valid Bearer token / API key.";
            case FORBIDDEN, CREDENTIALING_REQUIRED -> "Contact your organization admin to obtain the required role or credentialing.";
            case EMPLOYEE_NOT_FOUND, EMPLOYER_NOT_FOUND, VERIFICATION_NOT_FOUND, REPORT_NOT_FOUND,
                 DISPUTE_NOT_FOUND, BATCH_NOT_FOUND, API_KEY_NOT_FOUND, NOT_FOUND ->
                    "Verify the identifier and retry, or search the directory for the correct record.";
            case IDENTITY_NO_HIT -> "Confirm the applicant details; create the record or route to manual verification.";
            case IDENTITY_NO_MATCH, INSUFFICIENT_DATA -> "Review the attribute comparison and route to manual verification if needed.";
            case MANUAL_REVIEW_REQUIRED -> "Assign the case in the manual verification queue.";
            case PROVIDER_UNAVAILABLE -> "Retry shortly; the orchestrator will fall back to REVIEW_REQUIRED.";
            case RATE_LIMITED -> "Back off and retry with exponential delay.";
            default -> "Retry the operation; if it persists contact platform support.";
        };
    }
}
