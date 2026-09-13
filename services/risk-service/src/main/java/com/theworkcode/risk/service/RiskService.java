package com.theworkcode.risk.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.risk.engine.RiskEngine;
import com.theworkcode.risk.engine.RiskEngine.Assessment;
import com.theworkcode.risk.engine.RiskEngine.Signal;
import com.theworkcode.risk.entity.RiskAnomalyEntity;
import com.theworkcode.risk.entity.RiskAssessmentEntity;
import com.theworkcode.risk.repository.RiskAnomalyRepository;
import com.theworkcode.risk.repository.RiskAssessmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskAssessmentRepository assessmentRepository;
    private final RiskAnomalyRepository anomalyRepository;

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @Transactional
    public Assessment analyze(Map<String, Object> request) {
        String verificationId = String.valueOf(request.getOrDefault("verificationId", ""));
        String employeeNumber = String.valueOf(request.getOrDefault("employeeNumber", ""));
        String applicantName = String.valueOf(request.getOrDefault("applicantName", ""));
        String employerName = String.valueOf(request.getOrDefault("employerName", ""));

        @SuppressWarnings("unchecked")
        Map<String, Object> ctx = request.get("subResults") instanceof Map m
                ? new java.util.HashMap<>((Map<String, Object>) m)
                : new java.util.HashMap<>(request);

        // duplicate / fraud detection heuristics on the synthetic index
        enrichWithHeuristics(employeeNumber, employerName, ctx);

        Assessment assessment = RiskEngine.analyze(ctx);

        // persist assessment
        RiskAssessmentEntity entity = new RiskAssessmentEntity();
        entity.setVerificationId(verificationId);
        entity.setEmployeeNumber(employeeNumber);
        entity.setApplicantName(applicantName);
        entity.setEmployerName(employerName);
        entity.setScore(assessment.score());
        entity.setBand(assessment.band());
        try {
            entity.setSignals(MAPPER.writeValueAsString(
                    Map.of("signals", assessment.signals())));
        } catch (Exception ignored) {
            // best effort
        }
        assessmentRepository.save(entity);

        // persist anomalies for HIGH/CRITICAL negative signals
        for (Signal s : assessment.negatives()) {
            if (s.severity() >= 4) {
                RiskAnomalyEntity anomaly = new RiskAnomalyEntity();
                anomaly.setRuleCode(s.code());
                anomaly.setSeverity(s.severity() >= 5 ? "CRITICAL" : "HIGH");
                anomaly.setTitle(s.code().replace('_', ' ') + " detected");
                anomaly.setExplanation(s.explanation());
                anomaly.setEmployeeNumber(employeeNumber);
                anomaly.setEmployerName(employerName);
                anomaly.setVerificationId(verificationId);
                anomaly.setRecommendedAction(s.recommendedAction());
                anomaly.setStatus("OPEN");
                anomaly.setDetectedAt(java.time.OffsetDateTime.now());
                anomalyRepository.save(anomaly);
            }
        }

        log.info("risk_analyzed verificationId={} score={} band={} negatives={}",
                verificationId, assessment.score(), assessment.band(), assessment.negatives().size());
        return assessment;
    }

    /** Deterministic heuristics against the synthetic employee index. */
    private void enrichWithHeuristics(String employeeNumber, String employerName, Map<String, Object> ctx) {
        if (employeeNumber != null && employeeNumber.startsWith("DEMO-FRAUD")) {
            ctx.put("duplicateSuspected", true);
            ctx.put("highVerificationVelocity", true);
            ctx.put("suspiciousRequestPattern", true);
        }
        if (employeeNumber != null && employeeNumber.startsWith("DEMO-INCOME")) {
            ctx.put("incomeVariance", 0.62);
        }
        if (employerName != null && (employerName.contains("Saffron") || employerName.contains("Marigold"))) {
            ctx.put("employerFlagged", true);
        }
    }

    @Transactional(readOnly = true)
    public Assessment getByVerificationId(String verificationId) {
        RiskAssessmentEntity e = assessmentRepository.findTopByVerificationIdOrderByCreatedAtDesc(verificationId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                        "No risk assessment for verification '" + verificationId + "'."));
        List<Signal> signals = List.of();
        if (e.getSignals() != null) {
            try {
                var node = MAPPER.readTree(e.getSignals());
                signals = MAPPER.readValue(node.get("signals").toString(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<Signal>>() {
                        });
            } catch (Exception ignored) {
                // best effort
            }
        }
        return new Assessment(e.getScore(), e.getBand(), signals);
    }

    @Transactional(readOnly = true)
    public Page<RiskAnomalyEntity> anomalies(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        return anomalyRepository.findAllByOrderByDetectedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public long openAnomalies() {
        return anomalyRepository.countByStatus("OPEN");
    }

    @Transactional(readOnly = true)
    public long criticalAnomalies() {
        return anomalyRepository.countBySeverity("CRITICAL");
    }

    /** Demo seeding: generate a fixed set of anomaly events for the risk screens. */
    @Transactional
    public void seedDemoAnomaliesIfEmpty() {
        if (anomalyRepository.count() > 0) {
            return;
        }
        List<Signal> demo = List.of(
                new Signal("DUPLICATE_IDENTITY", "duplicate", 5, true,
                        "Employee ID DEMO-FRAUD-001 associated with two identities across employers.",
                        "Route to manual review and notify the fraud team."),
                new Signal("INCOME_ANOMALY", "income", 4, true,
                        "Gross pay variance 62% exceeds the 45% anomaly threshold for DEMO-INCOME-001.",
                        "Request recent payslips or route to manual verification."),
                new Signal("HIGH_VERIFICATION_VELOCITY", "duplicate", 4, true,
                        "4 verifications from 3 organizations for the same subject within 48 hours.",
                        "Review requester organizations."),
                new Signal("EMPLOYER_FLAGGED", "employment", 3, true,
                        "Saffron Payroll Services is PENDING_REVIEW in the trust register.",
                        "Use manual verification until trusted."),
                new Signal("REPEATED_FAILED_IDENTITY", "request", 4, true,
                        "6 failed identity attempts for a single applicant within one day.",
                        "Apply step-up verification."));
        int[] severities = { 5, 4, 4, 3, 4 };
        for (int i = 0; i < demo.size(); i++) {
            Signal s = demo.get(i);
            RiskAnomalyEntity anomaly = new RiskAnomalyEntity();
            anomaly.setRuleCode(s.code());
            anomaly.setSeverity(severities[i] >= 5 ? "CRITICAL" : severities[i] == 4 ? "HIGH" : "MEDIUM");
            anomaly.setTitle(s.code().replace('_', ' ') + " (seeded demo)");
            anomaly.setExplanation(s.explanation());
            anomaly.setRecommendedAction(s.recommendedAction());
            anomaly.setStatus("OPEN");
            anomaly.setDetectedAt(java.time.OffsetDateTime.now().minusHours(i * 7L));
            anomalyRepository.save(anomaly);
        }
        log.info("risk_seed_complete anomalies={}", demo.size());
    }
}
