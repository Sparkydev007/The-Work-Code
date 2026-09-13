package com.theworkcode.identity.provider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.identity.dto.IdentityDtos.IdentityVerifyRequest;
import com.theworkcode.identity.dto.IdentityDtos.IdentityVerifyResponse;
import com.theworkcode.identity.engine.IdentityMatchingEngine;
import com.theworkcode.identity.engine.IdentityMatchingEngine.MatchOutcome;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Demo provider: resolves the candidate synthetic record(s) from
 * employee-service, then runs the deterministic matching engine.
 *
 * Failure mode: if employee-service is unreachable the provider degrades to
 * NO_HIT semantics via PROVIDER_UNAVAILABLE (orchestrator routes to review).
 */
@Slf4j
@Component
public class DemoIdentityProvider implements IdentityVerificationProvider {

    private record EmployeeHit(String employeeNumber, String name, String employerName, String email) {
    }

    private record EmployeeSearchResponse(boolean success, Data data) {
    }

    private record Data(List<EmployeeHit> content) {
    }

    private final RestClient employeeClient;

    public DemoIdentityProvider(
            @Value("${workcode.employee-service-url:http://localhost:8082}") String employeeServiceUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(4));
        this.employeeClient = RestClient.builder()
                .baseUrl(employeeServiceUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public IdentityVerifyResponse verify(IdentityVerifyRequest request) {
        List<IdentityMatchingEngine.Candidate> candidates = findCandidates(request);
        MatchOutcome outcome = IdentityMatchingEngine.verify(
                request.name(), request.employeeNumber(), request.email(), request.employer(), candidates);
        return toResponse(outcome);
    }

    private List<IdentityMatchingEngine.Candidate> findCandidates(IdentityVerifyRequest request) {
        // Candidate resolution strategy:
        //  1. exact employee number hit
        //  2. name search hits (up to 10)
        //  3. otherwise NO_HIT
        try {
            if (request.employeeNumber() != null && !request.employeeNumber().isBlank()) {
                EmployeeSearchResponse byId = employeeClient.get()
                        .uri(uri -> uri.path("/api/v1/employees")
                                .queryParam("search", request.employeeNumber())
                                .queryParam("size", 5)
                                .queryParam("masked", false)
                                .build())
                        .retrieve()
                        .body(EmployeeSearchResponse.class);
                if (byId != null && byId.data() != null && !byId.data().content().isEmpty()) {
                    return byId.data().content().stream()
                            .map(e -> new IdentityMatchingEngine.Candidate(e.employeeNumber(), e.name(),
                                    e.employerName(), e.email()))
                            .toList();
                }
            }
            if (request.name() != null && !request.name().isBlank()) {
                EmployeeSearchResponse byName = employeeClient.get()
                        .uri(uri -> uri.path("/api/v1/employees")
                                .queryParam("search", request.name())
                                .queryParam("size", 10)
                                .queryParam("masked", false)
                                .build())
                        .retrieve()
                        .body(EmployeeSearchResponse.class);
                if (byName != null && byName.data() != null && !byName.data().content().isEmpty()) {
                    return byName.data().content().stream()
                            .map(e -> new IdentityMatchingEngine.Candidate(e.employeeNumber(), e.name(),
                                    e.employerName(), e.email()))
                            .toList();
                }
            }
            return List.of();
        } catch (Exception e) {
            log.error("employee_service_call_failed during identity candidate resolution", e);
            throw new ApiException(ErrorCode.PROVIDER_UNAVAILABLE,
                    "Employee index unavailable; identity verification cannot complete.");
        }
    }

    private IdentityVerifyResponse toResponse(MatchOutcome outcome) {
        return new IdentityVerifyResponse(
                null,
                outcome.result(),
                outcome.confidence(),
                round(outcome.scores().name()),
                round(outcome.scores().employer()),
                round(outcome.scores().employeeId()),
                round(outcome.scores().email()),
                outcome.matchedEmployeeNumber(),
                outcome.breakdown(),
                java.time.OffsetDateTime.now());
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
