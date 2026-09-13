package com.theworkcode.common.api;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handling shared by all services. Produces the canonical
 * error envelope with requestId, error code, message and recommended action.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorBody> handleApiException(ApiException ex) {
        log.warn("api_error code={} message={}", ex.errorCode(), ex.getMessage());
        return ResponseEntity.status(ex.errorCode().httpStatus())
                .body(ApiErrorBody.of(ex.errorCode(), ex.getMessage(), ex.details()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorBody> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
                        (a, b) -> a));
        return ResponseEntity.badRequest()
                .body(ApiErrorBody.of(ErrorCode.INVALID_REQUEST, "Request validation failed.", fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorBody> handleUnexpected(Exception ex) {
        log.error("unhandled_exception", ex);
        return ResponseEntity.internalServerError()
                .body(ApiErrorBody.of(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.defaultMessage(), Map.of()));
    }
}
