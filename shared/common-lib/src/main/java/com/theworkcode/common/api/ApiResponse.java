package com.theworkcode.common.api;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard API success envelope.
 *
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... },
 *   "requestId": "REQ-12345"
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, String requestId, Instant timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, com.theworkcode.common.correlation.RequestContext.getRequestId(), Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data, String requestId) {
        return new ApiResponse<>(true, data, requestId, Instant.now());
    }
}
