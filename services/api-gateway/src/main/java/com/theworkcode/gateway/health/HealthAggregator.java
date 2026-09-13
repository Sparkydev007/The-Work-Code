package com.theworkcode.gateway.health;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Aggregates {@code /actuator/health} from every downstream service into the
 * gateway health endpoint {@code GET /api/health}.
 *
 * <pre>
 * {
 *   "status": "UP",
 *   "services": { "identity": "UP", "employee": "UP", ... },
 *   "checkedAt": "..."
 * }
 * </pre>
 */
@Slf4j
@Component
public class HealthAggregator {

    private record Target(String name, String baseUrl) {
    }

    private static final java.util.List<Target> TARGETS = java.util.List.of(
            new Target("identity", "http://localhost:8081"),
            new Target("employee", "http://localhost:8082"),
            new Target("employer", "http://localhost:8083"),
            new Target("employment", "http://localhost:8084"),
            new Target("income", "http://localhost:8085"),
            new Target("verification", "http://localhost:8086"),
            new Target("risk", "http://localhost:8087"),
            new Target("report", "http://localhost:8088"),
            new Target("audit", "http://localhost:8089"),
            new Target("batch", "http://localhost:8090"),
            new Target("notification", "http://localhost:8091"));

    private final WebClient client;

    public HealthAggregator() {
        this.client = WebClient.builder()
                .build();
    }

    private record ServiceStatus(String name, String status) {
    }

    public Mono<ServerResponse> health(ServerRequest request) {
        long start = System.nanoTime();

        Flux<ServiceStatus> checks = Flux.fromIterable(TARGETS)
                .flatMap(target -> client.get()
                        .uri(target.baseUrl() + "/actuator/health")
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(body -> new ServiceStatus(target.name(), parseStatus(body)))
                        .onErrorReturn(new ServiceStatus(target.name(), "DOWN"))
                        .timeout(Duration.ofSeconds(3)));

        return checks
                .collectList()
                .flatMap(entries -> {
                    Map<String, String> services = new LinkedHashMap<>();
                    for (ServiceStatus e : entries) {
                        services.put(e.name(), e.status());
                    }
                    boolean anyDown = services.containsValue("DOWN");
                    String overall = anyDown ? "DEGRADED" : "UP";

                    long latencyMs = (System.nanoTime() - start) / 1_000_000;
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("success", true);
                    body.put("status", overall);
                    body.put("services", services);
                    body.put("latencyMs", latencyMs);
                    body.put("checkedAt", Instant.now().toString());
                    body.put("requestId", com.theworkcode.common.correlation.RequestContext.getRequestId());
                    return ServerResponse.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(body);
                })
                .onErrorResume(e -> {
                    log.error("health_aggregation_failed", e);
                    Map<String, Object> body = Map.of(
                            "success", false,
                            "status", "DOWN",
                            "error", String.valueOf(e.getMessage()));
                    return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(body);
                });
    }

    private String parseStatus(String actuatorBody) {
        if (actuatorBody == null) {
            return "DOWN";
        }
        if (actuatorBody.contains("\"UP\"")) {
            return "UP";
        }
        if (actuatorBody.contains("\"DOWN\"")) {
            return "DOWN";
        }
        if (actuatorBody.contains("\"DEGRADED\"")) {
            return "DEGRADED";
        }
        return "UNKNOWN";
    }
}
