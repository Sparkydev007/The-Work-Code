package com.theworkcode.common.api;

/**
 * Canonical error codes. Every service maps its exceptions onto these so the
 * frontend can present consistent error UX.
 */
public enum ErrorCode {
    INVALID_REQUEST(400, "The request payload failed validation."),
    UNAUTHORIZED(401, "Authentication is required."),
    FORBIDDEN(403, "You do not have permission to perform this action."),
    NOT_FOUND(404, "The requested resource was not found."),
    CONFLICT(409, "The request conflicts with existing state."),

    EMPLOYEE_NOT_FOUND(404, "Employee could not be found."),
    EMPLOYER_NOT_FOUND(404, "Employer could not be found."),
    VERIFICATION_NOT_FOUND(404, "Verification request was not found."),
    REPORT_NOT_FOUND(404, "Report was not found."),
    DISPUTE_NOT_FOUND(404, "Dispute was not found."),
    BATCH_NOT_FOUND(404, "Batch job was not found."),
    API_KEY_NOT_FOUND(404, "API key was not found."),

    IDENTITY_NO_HIT(404, "No identity record matched the supplied identifiers."),
    IDENTITY_NO_MATCH(422, "Identity attributes did not match the record on file."),
    MANUAL_REVIEW_REQUIRED(202, "Automated verification insufficient - manual review required."),
    INSUFFICIENT_DATA(422, "The source has insufficient data for this verification."),
    CREDENTIALING_REQUIRED(403, "Organization credentialing is required before submitting verifications."),

    PROVIDER_UNAVAILABLE(503, "The verification provider is unavailable."),
    REPORT_GENERATION_FAILED(500, "Report generation failed."),
    WEBHOOK_DELIVERY_FAILED(502, "Webhook delivery failed."),

    RATE_LIMITED(429, "Too many requests - rate limit exceeded."),
    INTERNAL_ERROR(500, "An unexpected internal error occurred.");

    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
