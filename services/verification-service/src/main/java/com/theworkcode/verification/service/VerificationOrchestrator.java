package com.theworkcode.verification.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.verification.dto.VerificationDtos.VerificationDetail;
import com.theworkcode.verification.dto.VerificationDtos.VerificationPageResponse;
import com.theworkcode.verification.dto.VerificationDtos.VerificationSummary;
import com.theworkcode.verification.dto.VerificationDtos.WorkflowStep;
import com.theworkcode.verification.dto.VerificationDtos.CreateVerificationRequest;
import com.theworkcode.verification.dto.VerificationDtos.CreateVerificationResponse;
import com.theworkcode.verification.entity.VerificationRequestEntity;
import com.theworkcode.verification.entity.WorkflowEventEntity;
import com.theworkcode.verification.repository.VerificationRequestRepository;
import com.theworkcode.verification.repository.WorkflowEventRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/**
 * The Verification Orchestrator - coordinates the end-to-end workflow:
 *
 * <pre>
 * Request -> Identity -> Employment -> Income -> Risk -> Decision -> Report
 * </pre>
 *
 * Each step is persisted as a workflow event (observable), each downstream
 * call is failure-isolated (a failing step degrades the result to
 * REVIEW_REQUIRED instead of crashing), and demo failure injection is
 * supported via the {@code simulate} query parameter.
 */
@Slf4j
@Service
public class VerificationOrchestrator {

    private final VerificationRequestRepository repository;
    private final WorkflowEventRepository eventRepository;
    private final RestClient identityClient;
    private final RestClient employmentClient;
    private final RestClient incomeClient;
    private final RestClient riskClient;
    private final RestClient reportClient;

    public VerificationOrchestrator(
            VerificationRequestRepository repository,
            WorkflowEventRepository eventRepository,
            @Value("${workcode.identity-service-url:http://localhost:8081}") String identityUrl,
            @Value("${workcode.employment-service-url:http://localhost:8084}") String employmentUrl,
            @Value("${workcode.income-service-url:http://localhost:8085}") String incomeUrl,
            @Value("${workcode.risk-service-url:http://localhost:8087}") String riskUrl,
            @Value("${workcode.report-service-url:http://localhost:8088}") String reportUrl) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.identityClient = buildClient(identityUrl);
        this.employmentClient = buildClient(employmentUrl);
        this.incomeClient = buildClient(incomeUrl);
        this.riskClient = buildClient(riskUrl);
        this.reportClient = buildClient(reportUrl);
    }

    private static RestClient buildClient(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(6));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Transactional
    public CreateVerificationResponse create(CreateVerificationRequest request, String requestedBy,
            String organizationId, String simulate) {
        // Idempotency: same key returns the original request instead of duplicating.
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var existing = repository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                return new CreateVerificationResponse(existing.get().getId(),
                        existing.get().getVerificationCode(), existing.get().getStatus(),
                        "Idempotent replay - existing verification returned.");
            }
        }

        String code = "VER-" + Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits()), 36)
                .toUpperCase().substring(0, 6);

        VerificationRequestEntity e = new VerificationRequestEntity();
        e.setVerificationCode(code);
        e.setIdempotencyKey(request.idempotencyKey());
        e.setOrganizationId(organizationId);
        e.setRequestedBy(requestedBy);
        e.setVerificationType(request.verificationType());
        e.setPurpose(request.purpose());
        e.setApplicantName(request.applicantName());
        e.setApplicantEmail(request.applicantEmail());
        e.setEmployeeNumber(request.employeeNumber());
        e.setEmployerName(request.employerName());
        e.setLookbackMonths(request.lookbackMonths());
        e.setStatus(VerificationRequestEntity.CREATED);
        try {
            if (request.applicantDob() != null && !request.applicantDob().isBlank()) {
                e.setApplicantDob(LocalDate.parse(request.applicantDob()));
            }
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "applicantDob must be ISO format (yyyy-MM-dd).");
        }
        e = repository.save(e);
        log.info("verification_created id={} code={} type={}", e.getId(), code, request.verificationType());

        // Process synchronously (demo). Production: emit event to queue (Kafka/Pub-Sub documented).
        process(e, simulate);

        return new CreateVerificationResponse(e.getId(), e.getVerificationCode(), e.getStatus(),
                "Verification processed.");
    }

    // ------------------------------------------------------------------
    // workflow
    // ------------------------------------------------------------------

    @Transactional
    public void process(VerificationRequestEntity e, String simulate) {
        e.setStatus(VerificationRequestEntity.PROCESSING);
        repository.save(e);

        Map<String, Object> subResults = new HashMap<>();
        boolean needsReview = false;
        double minConfidence = 100.0;

        try {
            // -------- Step: identity --------
            step(e, "IDENTITY_MATCHING", "RUNNING", null);
            e.setStatus(VerificationRequestEntity.IDENTITY_CHECK);
            repository.save(e);
            if ("IDENTITY".equalsIgnoreCase(simulate)) {
                throw new ApiException(ErrorCode.PROVIDER_UNAVAILABLE, "Simulated identity failure.");
            }
            IdentityCall identity = callIdentity(e);
            subResults.put("identity", identity.data());
            step(e, "IDENTITY_MATCHING", "DONE", identity.result());
            if (!"MATCH".equals(identity.result())) {
                needsReview = true;
            }
            minConfidence = Math.min(minConfidence, identity.confidence());
            e.setIdentityRef(identity.ref());

            // -------- Step: employment --------
            if (!"EMPLOYMENT".equalsIgnoreCase(simulate)) {
                step(e, "EMPLOYMENT_LOOKUP", "RUNNING", null);
                e.setStatus(VerificationRequestEntity.EMPLOYMENT_CHECK);
                repository.save(e);
                try {
                    EmploymentCall employment = callEmployment(e);
                    subResults.put("employment", employment.data());
                    step(e, "EMPLOYMENT_LOOKUP", "DONE", employment.result());
                    minConfidence = Math.min(minConfidence, employment.confidence());
                    e.setEmploymentRef(employment.ref());
                    if (!"VERIFIED".equals(employment.result())) {
                        needsReview = true;
                    }
                } catch (Exception ex) {
                    log.warn("employment_step_failed code={}", e.getVerificationCode(), ex);
                    subResults.put("employment", Map.of("error", "Employment service unavailable"));
                    step(e, "EMPLOYMENT_LOOKUP", "FAILED", "Service unavailable");
                    needsReview = true;
                }
            } else {
                step(e, "EMPLOYMENT_LOOKUP", "FAILED", "Simulated failure");
                needsReview = true;
            }

            // -------- Step: income (only for income / combined types) --------
            boolean wantsIncome = e.getVerificationType().contains("INCOME");
            if (wantsIncome && !"INCOME".equalsIgnoreCase(simulate)) {
                step(e, "INCOME_LOOKUP", "RUNNING", null);
                e.setStatus(VerificationRequestEntity.INCOME_CHECK);
                repository.save(e);
                try {
                    IncomeCall income = callIncome(e);
                    subResults.put("income", income.data());
                    step(e, "INCOME_LOOKUP", "DONE", income.result());
                    minConfidence = Math.min(minConfidence, income.confidence());
                    e.setIncomeRef(income.ref());
                    if (!"VERIFIED".equals(income.result())) {
                        needsReview = true;
                    }
                } catch (Exception ex) {
                    log.warn("income_step_failed code={}", e.getVerificationCode(), ex);
                    subResults.put("income", Map.of("error", "Income service unavailable"));
                    step(e, "INCOME_LOOKUP", "FAILED", "Service unavailable");
                    needsReview = true;
                }
            } else if (wantsIncome) {
                step(e, "INCOME_LOOKUP", "FAILED", "Simulated failure");
                needsReview = true;
            }

            // -------- Step: risk --------
            step(e, "RISK_ANALYSIS", "RUNNING", null);
            e.setStatus(VerificationRequestEntity.RISK_CHECK);
            repository.save(e);
            int riskScore = 12;
            String riskBand = "LOW";
            try {
                RiskCall risk = callRisk(e, subResults);
                riskScore = risk.score();
                riskBand = risk.band();
                subResults.put("risk", risk.data());
                step(e, "RISK_ANALYSIS", "DONE", riskBand + " (" + riskScore + ")");
                if (riskScore >= 60) {
                    needsReview = true;
                }
            } catch (Exception ex) {
                log.warn("risk_step_failed code={}", e.getVerificationCode(), ex);
                step(e, "RISK_ANALYSIS", "SKIPPED", "Risk engine unavailable - defaulted LOW");
            }
            e.setRiskScore(riskScore);
            e.setRiskBand(riskBand);

            // -------- Step: decision + report --------
            String result;
            String finalStatus;
            if (minConfidence < 60 && "NO_HIT".equals(String.valueOf(subResults.getOrDefault("identityResult", "")))) {
                result = "NOT VERIFIED";
                finalStatus = VerificationRequestEntity.COMPLETED;
            } else if (needsReview) {
                result = "VERIFIED WITH REVIEW";
                finalStatus = VerificationRequestEntity.REVIEW_REQUIRED;
            } else {
                result = "VERIFIED";
                finalStatus = VerificationRequestEntity.COMPLETED;
            }
            e.setResult(result);
            e.setConfidence(java.math.BigDecimal.valueOf(Math.max(0, Math.min(100, minConfidence))));

            step(e, "REPORT_GENERATION", "RUNNING", null);
            try {
                String reportId = callReport(e, result);
                e.setReportRef(reportId);
                step(e, "REPORT_GENERATION", "DONE", "Report " + reportId);
            } catch (Exception ex) {
                log.warn("report_generation_failed code={}", e.getVerificationCode(), ex);
                step(e, "REPORT_GENERATION", "FAILED", "Report engine unavailable");
            }

            e.setStatus(finalStatus);
            e.setCompletedAt(OffsetDateTime.now());
            repository.save(e);
            step(e, "DECISION", "DONE", result);

        } catch (Exception ex) {
            log.error("verification_failed code={}", e.getVerificationCode(), ex);
            e.setStatus(VerificationRequestEntity.FAILED);
            e.setFailureReason(ex.getMessage() == null ? "Processing failed" : ex.getMessage());
            e.setCompletedAt(OffsetDateTime.now());
            repository.save(e);
        }
    }

    private void step(VerificationRequestEntity e, String stepName, String status, String detail) {
        WorkflowEventEntity event = new WorkflowEventEntity();
        event.setRequestId(e.getId());
        event.setStep(stepName);
        event.setStatus(status);
        event.setDetail(detail);
        eventRepository.save(event);
    }

    // ------------------------------------------------------------------
    // downstream calls (tolerant records)
    // ------------------------------------------------------------------

    private record IdentityCall(String result, double confidence, String ref, Map<String, Object> data) {
    }

    @SuppressWarnings("unchecked")
    private IdentityCall callIdentity(VerificationRequestEntity e) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", e.getApplicantName());
        body.put("employeeNumber", e.getEmployeeNumber());
        body.put("email", e.getApplicantEmail());
        body.put("employer", e.getEmployerName());

        Map<String, Object> resp = identityClient.post()
                .uri("/api/v1/identity/verify")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);
        Map<String, Object> data = resp == null ? Map.of() : castMap(resp.get("data"));
        String result = String.valueOf(data.getOrDefault("result", "NO_HIT"));
        double confidence = data.get("confidence") instanceof Number n ? n.doubleValue() : 0;
        String ref = String.valueOf(data.getOrDefault("verificationId", ""));
        return new IdentityCall(result, confidence, ref, data);
    }

    private record EmploymentCall(String result, double confidence, String ref, Map<String, Object> data) {
    }

    @SuppressWarnings("unchecked")
    private EmploymentCall callEmployment(VerificationRequestEntity e) {
        Map<String, Object> body = new HashMap<>();
        body.put("employeeNumber", e.getEmployeeNumber());
        body.put("employerName", e.getEmployerName());
        body.put("lookbackMonths", e.getLookbackMonths());

        Map<String, Object> resp = employmentClient.post()
                .uri("/api/v1/employment/verifications")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);
        Map<String, Object> data = resp == null ? Map.of() : castMap(resp.get("data"));
        String result = String.valueOf(data.getOrDefault("result", "NO_RECORD"));
        double confidence = data.get("confidence") instanceof Number n ? n.doubleValue() : 0;
        String ref = String.valueOf(data.getOrDefault("id", ""));
        return new EmploymentCall(result, confidence, ref, data);
    }

    private record IncomeCall(String result, double confidence, String ref, Map<String, Object> data) {
    }

    @SuppressWarnings("unchecked")
    private IncomeCall callIncome(VerificationRequestEntity e) {
        Map<String, Object> body = new HashMap<>();
        body.put("employeeNumber", e.getEmployeeNumber());
        body.put("employerName", e.getEmployerName());
        body.put("lookbackMonths", e.getLookbackMonths());

        Map<String, Object> resp = incomeClient.post()
                .uri("/api/v1/income/verifications")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);
        Map<String, Object> data = resp == null ? Map.of() : castMap(resp.get("data"));
        String result = String.valueOf(data.getOrDefault("result", "NO_RECORD"));
        double confidence = data.get("confidence") instanceof Number n ? n.doubleValue() : 0;
        String ref = String.valueOf(data.getOrDefault("id", ""));
        return new IncomeCall(result, confidence, ref, data);
    }

    private record RiskCall(int score, String band, Map<String, Object> data) {
    }

    @SuppressWarnings("unchecked")
    private RiskCall callRisk(VerificationRequestEntity e, Map<String, Object> subResults) {
        Map<String, Object> body = new HashMap<>();
        body.put("verificationId", String.valueOf(e.getId()));
        body.put("employeeNumber", e.getEmployeeNumber());
        body.put("applicantName", e.getApplicantName());
        body.put("employerName", e.getEmployerName());
        body.put("verificationType", e.getVerificationType());
        body.put("subResults", subResults);

        Map<String, Object> resp = riskClient.post()
                .uri("/api/v1/risk/analyze")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);
        Map<String, Object> data = resp == null ? Map.of() : castMap(resp.get("data"));
        int score = data.get("score") instanceof Number n ? n.intValue() : 12;
        String band = String.valueOf(data.getOrDefault("band", "LOW"));
        return new RiskCall(score, band, data);
    }

    private String callReport(VerificationRequestEntity e, String result) {
        Map<String, Object> body = new HashMap<>();
        body.put("verificationId", String.valueOf(e.getId()));
        body.put("verificationCode", e.getVerificationCode());
        body.put("verificationType", e.getVerificationType());
        body.put("applicantName", e.getApplicantName());
        body.put("employerName", e.getEmployerName());
        body.put("result", result);

        Map<String, Object> resp = reportClient.post()
                .uri("/api/v1/reports")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);
        Map<String, Object> data = resp == null ? Map.of() : castMap(resp.get("data"));
        return String.valueOf(data.getOrDefault("reportCode", "RPT-UNAVAILABLE"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : Map.of();
    }

    // ------------------------------------------------------------------
    // queries
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public VerificationDetail get(UUID id) {
        VerificationRequestEntity e = repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.VERIFICATION_NOT_FOUND,
                        "Verification '" + id + "' was not found."));
        return toDetail(e);
    }

    @Transactional(readOnly = true)
    public VerificationPageResponse search(String status, String type, String search, int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(
                Math.max(0, page), Math.min(200, Math.max(1, size)),
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        var result = repository.search(blankToNull(status), blankToNull(type), blankToNull(search), pageable);
        List<VerificationSummary> content = result.getContent().stream().map(v -> new VerificationSummary(
                v.getId(), v.getVerificationCode(), v.getApplicantName(), v.getEmployerName(),
                v.getVerificationType(), v.getStatus(), v.getResult(),
                v.getConfidence() == null ? null : v.getConfidence().doubleValue(),
                v.getRiskScore(), v.getRiskBand(), v.getCreatedAt())).toList();
        return new VerificationPageResponse(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public CreateVerificationResponse rerun(UUID id) {
        VerificationRequestEntity e = repository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.VERIFICATION_NOT_FOUND,
                        "Verification '" + id + "' was not found."));
        e.setStatus(VerificationRequestEntity.CREATED);
        e.setFailureReason(null);
        repository.save(e);
        process(e, null);
        return new CreateVerificationResponse(e.getId(), e.getVerificationCode(), e.getStatus(), "Re-run complete.");
    }

    private VerificationDetail toDetail(VerificationRequestEntity e) {
        List<WorkflowStep> steps = eventRepository.findByRequestIdOrderByOccurredAtAsc(e.getId()).stream()
                .map(w -> new WorkflowStep(w.getStep(), w.getStatus(), w.getDetail(), w.getOccurredAt()))
                .toList();
        return new VerificationDetail(e.getId(), e.getVerificationCode(), e.getVerificationType(), e.getPurpose(),
                e.getApplicantName(), e.getApplicantEmail(), e.getEmployeeNumber(), e.getEmployerName(),
                e.getLookbackMonths(), e.getStatus(), e.getResult(),
                e.getConfidence() == null ? null : e.getConfidence().doubleValue(),
                e.getRiskScore(), e.getRiskBand(), e.getIdentityRef(), e.getEmploymentRef(), e.getIncomeRef(),
                e.getReportRef(), e.getFailureReason(), e.getCreatedAt(), e.getCompletedAt(), steps, Map.of());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
