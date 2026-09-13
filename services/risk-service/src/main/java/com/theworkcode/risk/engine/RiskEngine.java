package com.theworkcode.risk.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deterministic demo risk engine. Rules are transparent and documented -
 * an original implementation, not derived from any commercial provider.
 *
 * <p>Risk dimensions and weights:
 * <pre>
 *   Identity risk   30%
 *   Employment risk 25%
 *   Income risk     20%
 *   Request risk    15%
 *   Duplicate risk  10%
 * </pre>
 *
 * <p>Bands: 0-29 LOW, 30-59 MEDIUM, 60-79 HIGH, 80-100 CRITICAL.
 */
public final class RiskEngine {

    public record Signal(String code, String dimension, int severity /*1-5*/, boolean negative,
            String explanation, String recommendedAction) {
    }

    public record Assessment(int score, String band, List<Signal> signals) {

        public List<Signal> positives() {
            return signals.stream().filter(s -> !s.negative()).toList();
        }

        public List<Signal> negatives() {
            return signals.stream().filter(Signal::negative).toList();
        }
    }

    private RiskEngine() {
    }

    /**
     * Analyzes a verification context. Expected keys (all optional):
     * identityResult, identityConfidence, employmentResult, incomeResult,
     * duplicateSuspected, recentDuplicateRequest, employerMismatch.
     */
    public static Assessment analyze(Map<String, Object> ctx) {
        List<Signal> signals = new ArrayList<>();

        String identityResult = str(ctx, "identityResult", "MATCH");
        String employmentResult = str(ctx, "employmentResult", "VERIFIED");
        String incomeResult = str(ctx, "incomeResult", "VERIFIED");
        double identityConfidence = dbl(ctx, "identityConfidence", 95);
        boolean duplicateSuspected = bool(ctx, "duplicateSuspected", false);
        boolean recentDuplicateRequest = bool(ctx, "recentDuplicateRequest", false);
        boolean employerMismatch = bool(ctx, "employerMismatch", false);
        int failedAttempts = (int) dbl(ctx, "failedAttempts", 0);
        double incomeVariance = dbl(ctx, "incomeVariance", 0.0);

        // ---- Positive signals ----
        if ("MATCH".equals(identityResult)) {
            signals.add(new Signal("IDENTITY_MATCHED", "identity", 1, false,
                    "Identity attributes matched the record on file.", null));
        }
        if ("VERIFIED".equals(employmentResult)) {
            signals.add(new Signal("EMPLOYMENT_VERIFIED", "employment", 1, false,
                    "Employment verified against the source record.", null));
        }
        if ("ACTIVE".equalsIgnoreCase(str(ctx, "employmentStatus", "ACTIVE"))) {
            signals.add(new Signal("ACTIVE_EMPLOYMENT", "employment", 1, false,
                    "Active employment status confirmed.", null));
        }
        if ("VERIFIED".equals(incomeResult)) {
            signals.add(new Signal("CONSISTENT_PAYROLL", "income", 1, false,
                    "Payroll history is present and internally consistent.", null));
        }

        // ---- Negative signals (10 documented rules) ----

        // Rule 1: same employee verified by many organizations unusually quickly (velocity)
        if (bool(ctx, "highVerificationVelocity", false)) {
            signals.add(new Signal("HIGH_VERIFICATION_VELOCITY", "duplicate", 4, true,
                    "Same employee was verified by multiple organizations in a short window.",
                    "Review requester organizations and contact the employee if needed."));
        }

        // Rule 2: same employee ID associated with multiple people
        if (duplicateSuspected) {
            signals.add(new Signal("DUPLICATE_IDENTITY", "duplicate", 5, true,
                    "Employee ID is associated with multiple distinct identities.",
                    "Route to manual review and notify the fraud team."));
        }

        // Rule 3: same email associated with multiple employee records
        if (bool(ctx, "sharedEmail", false)) {
            signals.add(new Signal("SHARED_EMAIL", "identity", 3, true,
                    "Same email address appears on multiple employee records.",
                    "Verify email ownership with the employer."));
        }

        // Rule 4: employer mismatch
        if (employerMismatch || "NOT_VERIFIED".equals(employmentResult)) {
            signals.add(new Signal("EMPLOYER_MISMATCH", "employment", 4, true,
                    "Employer supplied by the requester does not match the record on file.",
                    "Confirm the employer with the applicant."));
        }

        // Rule 5: income suddenly changes abnormally
        if (incomeVariance > 0.45) {
            signals.add(new Signal("INCOME_ANOMALY", "income", 4, true,
                    String.format("Gross pay variance %.0f%% exceeds the 45%% anomaly threshold.",
                            incomeVariance * 100),
                    "Request recent payslips or route to manual verification."));
        }

        // Rule 6: employment dates overlap suspiciously
        if (bool(ctx, "overlappingTenures", false)) {
            signals.add(new Signal("OVERLAPPING_TENURES", "employment", 4, true,
                    "Employment history contains overlapping full-time tenures.",
                    "Manually reconcile the employment timeline."));
        }

        // Rule 7: repeated failed identity requests
        if (failedAttempts >= 3) {
            signals.add(new Signal("REPEATED_FAILED_IDENTITY", "request", 3 + Math.min(2, failedAttempts / 5), true,
                    failedAttempts + " recent failed identity attempts for this applicant.",
                    "Apply step-up verification and monitor."));
        }

        // Rule 8: identity used from suspicious request pattern
        if (bool(ctx, "suspiciousRequestPattern", false)) {
            signals.add(new Signal("SUSPICIOUS_REQUEST_PATTERN", "request", 4, true,
                    "Request pattern matches known fraud heuristics (burst + rotating IPs in demo data).",
                    "Escalate to compliance review."));
        }

        // Rule 9: synthetic employer record flagged in demo dataset
        if (bool(ctx, "employerFlagged", false)) {
            signals.add(new Signal("EMPLOYER_FLAGGED", "employment", 3, true,
                    "Employer record is flagged in the demo trust register (PENDING_REVIEW).",
                    "Use manual verification until the contributor is trusted."));
        }

        // Rule 10: impossible payroll pattern
        if (bool(ctx, "impossiblePayroll", false)) {
            signals.add(new Signal("IMPOSSIBLE_PAYROLL", "income", 5, true,
                    "Payroll pattern is internally impossible (pay dates before hire or negative deductions).",
                    "Reject automated result; require manual verification."));
        }

        // Low identity confidence
        if (!"MATCH".equals(identityResult)) {
            signals.add(new Signal("IDENTITY_NOT_MATCHED", "identity", 4, true,
                    "Identity result was " + identityResult + ".",
                    "Review attribute comparison before proceeding."));
        } else if (identityConfidence < 92) {
            signals.add(new Signal("IDENTITY_LOW_CONFIDENCE", "identity", 2, true,
                    String.format("Identity confidence %.1f%% is below the 92%% comfort threshold.", identityConfidence),
                    "Consider step-up identity checks."));
        }

        if ("REVIEW".equals(incomeResult)) {
            signals.add(new Signal("INCOME_REVIEW", "income", 3, true,
                    "Income data is sparse or high-variance; treat as indicative only.",
                    "Request supporting documents."));
        }

        if (recentDuplicateRequest) {
            signals.add(new Signal("RECENT_DUPLICATE_REQUEST", "request", 2, true,
                    "A duplicate verification for this subject was requested recently.",
                    "Deduplicate before re-processing."));
        }

        // ---- Scoring ----
        // Dimension contributions are severity-weighted; the x2 amplifier lets a
        // single critical signal (severity 5) dominate its dimension, while pure
        // clean files stay at 0. Capped to [0, 100].
        double identityRisk = dimensionScore(signals, "identity", 30);
        double employmentRisk = dimensionScore(signals, "employment", 25);
        double incomeRisk = dimensionScore(signals, "income", 20);
        double requestRisk = dimensionScore(signals, "request", 15);
        double duplicateRisk = dimensionScore(signals, "duplicate", 10);

        int score = (int) Math.round(
                2 * (identityRisk + employmentRisk + incomeRisk + requestRisk + duplicateRisk));
        score = Math.max(0, Math.min(100, score));
        String band = score <= 29 ? "LOW" : score <= 59 ? "MEDIUM" : score <= 79 ? "HIGH" : "CRITICAL";

        return new Assessment(score, band, signals);
    }

    /** Severity-weighted dimension score; each negative signal contributes severity/5 * dimensionWeight. */
    private static double dimensionScore(List<Signal> signals, String dimension, double weight) {
        return signals.stream()
                .filter(s -> s.negative() && dimension.equals(s.dimension()))
                .mapToDouble(s -> weight * s.severity() / 5.0)
                .sum();
    }

    private static String str(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v == null ? def : String.valueOf(v);
    }

    private static double dbl(Map<String, Object> map, String key, double def) {
        Object v = map.get(key);
        return v instanceof Number n ? n.doubleValue() : def;
    }

    private static boolean bool(Map<String, Object> map, String key, boolean def) {
        Object v = map.get(key);
        return v instanceof Boolean b ? b : def;
    }
}
