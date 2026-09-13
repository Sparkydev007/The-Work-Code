package com.theworkcode.common.correlation;

/**
 * Thread-local holder for the correlation (request) ID. The API gateway
 * creates or propagates {@code X-Request-ID}; downstream services echo it in
 * every response and log line.
 */
public final class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private RequestContext() {
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static void clear() {
        REQUEST_ID.remove();
    }
}
