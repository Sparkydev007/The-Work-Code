package com.theworkcode.gateway.auth;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.theworkcode.common.security.JwtSupport;
import com.theworkcode.common.security.Role;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway-level security:
 * demo persona login, JWT validation, role header forwarding,
 * rate limiting, correlation ID propagation.
 *
 * Demo-only authentication. Production deployments must replace persona login
 * with real accounts, hashed credentials and refresh tokens.
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_FORWARDED_USER = "X-Forwarded-User";
    public static final String HEADER_FORWARDED_ROLE = "X-Forwarded-User-Role";
    public static final String HEADER_FORWARDED_NAME = "X-Forwarded-User-Name";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final int RATE_LIMIT_PER_MINUTE = 600;

    private final JwtSupport jwt;
    private final List<String> publicPaths;
    private final Map<String, Window> rateWindows = new ConcurrentHashMap<>();

    public JwtAuthFilter(
            @Value("${workcode.gateway.jwt-secret}") String jwtSecret,
            @Value("${workcode.gateway.public-paths}") List<String> publicPaths) {
        this.jwt = new JwtSupport(jwtSecret, 3600);
        this.publicPaths = publicPaths;
    }

    private record DemoUser(String username, String role, String orgId, String name) {
    }

    private static final Map<String, DemoUser> DEMO_USERS = Map.of(
            "admin", new DemoUser("admin", Role.ADMIN.name(), "ORG-8821", "Alex Morgan"),
            "analyst", new DemoUser("analyst", Role.ANALYST.name(), "ORG-8821", "Sam Whitfield"),
            "developer", new DemoUser("developer", Role.DEVELOPER.name(), "ORG-8821", "Devon Park"),
            "viewer", new DemoUser("viewer", Role.VIEWER.name(), "ORG-8821", "Riley Chen"));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        String requestId = request.getHeaders().getFirst(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = "REQ-" + UUID.randomUUID();
        }
        ServerHttpRequest withRequestId = request.mutate()
                .header(HEADER_REQUEST_ID, requestId)
                .build();

        if (isPublic(path)) {
            if (LOGIN_PATH.equals(path) && isPost(request)) {
                return handleLogin(exchange.mutate().request(withRequestId).build());
            }
            exchange.getResponse().getHeaders().add(HEADER_REQUEST_ID, requestId);
            return chain.filter(exchange.mutate().request(withRequestId).build());
        }

        Window window = rateWindows.computeIfAbsent(clientIp(request), k -> new Window());
        long nowMinute = System.currentTimeMillis() / 60_000;
        synchronized (window) {
            if (window.minute != nowMinute) {
                window.minute = nowMinute;
                window.count = new AtomicInteger(0);
            }
            if (window.count.incrementAndGet() > RATE_LIMIT_PER_MINUTE) {
                return reject(exchange, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED",
                        "Too many requests. Slow down and retry.", requestId);
            }
        }

        String token = bearerToken(request);
        var claims = token == null ? null : jwt.parse(token);
        if (claims == null) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                    "A valid Bearer token is required.", requestId);
        }

        String role = String.valueOf(claims.get(JwtSupport.CLAIM_ROLE));
        String user = claims.getSubject();
        String name = String.valueOf(claims.get(JwtSupport.CLAIM_NAME));

        ServerHttpRequest authorized = withRequestId.mutate()
                .header(HEADER_FORWARDED_USER, user)
                .header(HEADER_FORWARDED_ROLE, role)
                .header(HEADER_FORWARDED_NAME, name)
                .build();

        exchange.getResponse().getHeaders().add(HEADER_REQUEST_ID, requestId);
        return chain.filter(exchange.mutate().request(authorized).build());
    }

    private Mono<Void> handleLogin(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        String requestId = exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);

        return exchange.getRequest().getBody()
                .collectList()
                .map(dataBuffers -> {
                    byte[] bytes = toByteArray(dataBuffers);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    return extractJsonString(body, "username");
                })
                .flatMap(username -> {
                    DemoUser user = DEMO_USERS.get(username == null ? "" : username.toLowerCase());
                    if (user == null) {
                        return reject(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                                "Unknown demo persona. Use admin / analyst / developer / viewer.", requestId);
                    }
                    String token = jwt.issue(user.username(), user.role(), user.orgId(), user.name());
                    String json = loginResponse(token, user, requestId);
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
                    return response.writeWith(Mono.just(buffer));
                });
    }

    private String loginResponse(String token, DemoUser user, String requestId) {
        return "{\"success\":true,\"data\":{\"token\":\"" + token + "\",\"tokenType\":\"Bearer\","
                + "\"expiresIn\":3600,\"user\":{\"username\":\"" + user.username() + "\",\"role\":\""
                + user.role() + "\",\"organizationId\":\"" + user.orgId() + "\",\"name\":\""
                + user.name() + "\"}},\"requestId\":\"" + requestId + "\"}";
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String code, String message,
            String requestId) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().add(HEADER_REQUEST_ID, requestId);
        String json = "{\"success\":false,\"error\":{\"code\":\"" + code + "\",\"message\":\"" + message
                + "\"},\"requestId\":\"" + requestId + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private boolean isPublic(String path) {
        for (String p : publicPaths) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPost(ServerHttpRequest request) {
        return request.getMethod() != null && "POST".equalsIgnoreCase(request.getMethod().name());
    }

    private String bearerToken(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst(HEADER_AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    private String clientIp(ServerHttpRequest request) {
        String fwd = request.getHeaders().getFirst("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) {
            return fwd.split(",")[0].trim();
        }
        return request.getRemoteAddress() == null ? "unknown"
                : String.valueOf(request.getRemoteAddress().getAddress());
    }

    private byte[] toByteArray(List<DataBuffer> dataBuffers) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (DataBuffer db : dataBuffers) {
            byte[] bytes = new byte[db.readableByteCount()];
            db.read(bytes);
            out.writeBytes(bytes);
            org.springframework.core.io.buffer.DataBufferUtils.release(db);
        }
        return out.toByteArray();
    }

    private static String extractJsonString(String body, String field) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(body);
        return m.find() ? m.group(1) : null;
    }

    private static final class Window {
        private long minute = System.currentTimeMillis() / 60_000;
        private AtomicInteger count = new AtomicInteger(0);
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
