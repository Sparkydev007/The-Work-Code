package com.theworkcode.common.api;

import java.util.Map;

/**
 * Business exception carrying a canonical {@link ErrorCode} plus optional
 * details rendered to the client.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Map<String, Object> details;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.details = Map.of();
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = Map.of();
    }

    public ApiException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public static ApiException notFound(ErrorCode code, String message) {
        return new ApiException(code, message);
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Map<String, Object> details() {
        return details;
    }
}
