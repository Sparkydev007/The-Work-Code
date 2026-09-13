package com.theworkcode.common.correlation;

import java.io.IOException;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads {@code X-Request-ID} (creating one if absent), stores it in
 * {@link RequestContext} for the duration of the request and echoes it back
 * on every response.
 */
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter implements Ordered {

    public static final String HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = request.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = "REQ-" + UUID.randomUUID();
        }
        RequestContext.setRequestId(requestId);
        response.setHeader(HEADER, requestId);
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("http method={} path={} status={} durationMs={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            RequestContext.clear();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
